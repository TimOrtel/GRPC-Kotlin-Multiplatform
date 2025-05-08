mod rawcoding;
mod rawpointer;

use crate::rawcoding::{DecodeMessage, EncodeMessage, RawBytesCodec, RpcOnMessageWrittenInternal};
use crate::rawpointer::RawPtr;
use once_cell::sync::Lazy;
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_void};
use std::ptr::null_mut;
use std::str::FromStr;
use tokio::runtime::Runtime;
use tokio::sync::mpsc::Receiver;
use tokio::sync::mpsc::Sender;
use tonic::client::Grpc;
use tonic::codegen::http::uri::PathAndQuery;
use tonic::codegen::tokio_stream::StreamExt;
use tonic::codegen::tokio_stream::wrappers::ReceiverStream;
use tonic::metadata::{KeyRef, MetadataKey, MetadataMap, MetadataValue};
use tonic::transport::Channel;
use tonic::{Code, IntoStreamingRequest, Request, Status};

static TOKIO_RT: Lazy<Runtime> = Lazy::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap()
});

type RpcOnDone = extern "C" fn(
    user_data: *mut c_void,
    status_code: i32,
    status_message: *const c_char,
    metadata: *mut RustMetadata,
    trailers: *mut RustMetadata,
);
type RpcOnMessageReceived = extern "C" fn(user_data: *mut c_void, message: *mut c_void);
type RpcOnInitialMetadataReceived =
    extern "C" fn(user_data: *mut c_void, metadata: *mut RustMetadata);

type RpcOnMessageWritten = extern "C" fn(user_data: *mut c_void);

/**
 * Holds the channel used to send messages into the RPC.
 */
pub struct RequestChannel {
    _sender: Option<Sender<RawPtr>>,
    _receiver: Option<Receiver<RawPtr>>,
}

/**
 * Holds the metadata of Rust.
 */
pub struct RustMetadata {
    _metadata: MetadataMap,
}

impl RequestChannel {
    pub fn close_sender(&mut self) {
        self._sender.take();
    }
}

/**
 * Holds the actual channel implementation in Rust
 */
pub struct RustChannel {
    _channel: Channel,
}

/**
 * Holds a handle to a task on which the rpc is running. Can be used to abort the task.
 */
pub struct RpcTask {
    _handle: tokio::task::JoinHandle<()>,
}

/**
 * Perform a general rpc. Uses BIDI streaming, but can be used for all types.
 */
#[unsafe(no_mangle)]
pub unsafe extern "C" fn rpc_implementation(
    channel: *mut RustChannel,
    path: *const c_char,
    metadata: *mut RustMetadata,
    request_channel: *mut RequestChannel,
    user_data: *mut c_void,
    serialize_request: EncodeMessage,
    deserialize_response: DecodeMessage,
    on_message_received: RpcOnMessageReceived,
    on_message_written: RpcOnMessageWritten,
    on_initial_metadata_received: RpcOnInitialMetadataReceived,
    on_done: RpcOnDone,
) -> *mut RpcTask {
    let on_done = move |user_data: RawPtr,
                        status: i32,
                        message: *const c_char,
                        metadata: *mut RustMetadata,
                        trailers: *mut RustMetadata| {
        on_done(user_data.0, status, message, metadata, trailers)
    };

    let on_message_received = move |user_data: RawPtr, message: RawPtr| {
        on_message_received(user_data.0, message.0);
    };

    let on_initial_metadata_received = move |user_data: RawPtr, metadata: *mut RustMetadata| {
        on_initial_metadata_received(user_data.0, metadata)
    };

    let on_done_error = move |user_data: RawPtr, err_str: &str| {
        on_done(
            user_data,
            i32::from(Code::Internal),
            CString::new(err_str).unwrap().into_raw(),
            null_mut(),
            null_mut(),
        );
    };

    let user_data = RawPtr(user_data);
    let request_channel = unsafe { request_channel.as_mut() };
    let channel = unsafe { channel.as_mut() };
    let metadata = unsafe { metadata.as_mut() };

    let path = unsafe {
        match CStr::from_ptr(path).to_str() {
            Ok(str) => str.to_string(),
            Err(_) => {
                on_done_error(user_data, "invalid path");
                return null_mut();
            }
        }
    };

    let handle = TOKIO_RT.spawn(async move {
        let on_done_impl = move |status_code: i32,
                                 status_message: &str,
                                 metadata: MetadataMap,
                                 trailers: Option<MetadataMap>| {
            on_done(
                user_data,
                status_code,
                CString::new(status_message).unwrap().into_raw(),
                Box::into_raw(Box::new(RustMetadata {
                    _metadata: metadata,
                })),
                match trailers {
                    Some(metadata_trailers) => Box::into_raw(Box::new(RustMetadata {
                        _metadata: metadata_trailers,
                    })),
                    None => null_mut(),
                },
            );
        };

        let on_done_status = move |status: Status, trailers: Option<MetadataMap>| {
            on_done_impl(
                i32::from(status.code()),
                status.message(),
                status.metadata().clone(),
                trailers,
            );
        };

        let path = PathAndQuery::from_maybe_shared(path).unwrap();

        let mut grpc = Grpc::new(channel.unwrap()._channel.clone());
        if let Err(err) = grpc.ready().await {
            on_done_error(user_data, err.to_string().as_str());
        }
        
        let mut request_stream: Request<ReceiverStream<RawPtr>> =
            if let Some(channel) = request_channel {
                if let Some(receiver) = channel._receiver.take() {
                    ReceiverStream::new(receiver).into_streaming_request()
                } else {
                    on_done_status(Status::new(Code::Internal, "no receiver channel"), None);
                    return;
                }
            } else {
                on_done_status(Status::new(Code::Internal, "no request channel"), None);
                return;
            };

        // Insert the metadata
        if let Some(metadata) = metadata {
            let md = metadata._metadata.clone();
            let target = request_stream.metadata_mut();

            for key in md.keys() {
                match key {
                    KeyRef::Ascii(ascii_key) => {
                        if let Some(value) = md.get(ascii_key) {
                            target.insert(ascii_key, value.clone());
                        }
                    }
                    // TODO: implement binary metadata
                    KeyRef::Binary(_) => {}
                }
            }
        }
        
        let result = grpc
            .streaming(
                request_stream,
                path,
                RawBytesCodec {
                    user_data,
                    encode: serialize_request,
                    decode: deserialize_response,
                    on_message_written: RpcOnMessageWrittenInternal {
                        on_message_written,
                        user_data,
                    },
                },
            )
            .await;
        
        match result {
            Ok(response) => {
                let (metadata, mut body, _) = response.into_parts();

                on_initial_metadata_received(
                    user_data,
                    Box::into_raw(Box::new(RustMetadata {
                        _metadata: metadata,
                    })),
                );

                loop {
                    match body.next().await {
                        Some(result) => match result {
                            Ok(message) => {
                                on_message_received(user_data, message);
                            }
                            Err(status) => {
                                on_done_status(status, None);
                                return;
                            }
                        },
                        None => {
                            break;
                        }
                    }
                }

                match body.trailers().await {
                    Ok(trailers) => on_done_impl(0, "", MetadataMap::new(), trailers),
                    Err(status) => {
                        on_done_status(status, None);
                    }
                };
            }
            Err(status) => {
                on_done_status(status, None);
            }
        }
    });
    
    Box::into_raw(Box::new(RpcTask { _handle: handle }))
}

/**
 * Create a new channel from the given host. Returns null if the channel could not be created.
 */
#[unsafe(no_mangle)]
pub extern "C" fn channel_create(host: *const c_char) -> *mut RustChannel {
    let host = unsafe {
        match CStr::from_ptr(host).to_str() {
            Ok(str) => str.to_string(),
            Err(_) => {
                return null_mut();
            }
        }
    };

    let _guard = TOKIO_RT.enter();

    match Channel::from_shared(host) {
        Ok(endpoint) => Box::into_raw(Box::new(RustChannel {
            _channel: endpoint.connect_lazy(),
        })),
        Err(_) => null_mut(),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn channel_free(channel: *mut RustChannel) {
    if channel.is_null() {
        return;
    }
    unsafe {
        drop(Box::from_raw(channel));
    }
}

/**
 * Create a new request channel which allows to send messages into a RPC
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_create() -> *mut RequestChannel {
    // A buffer of 1 is enough, as the Kotlin implementation always waits for the previous message to be written to the wire first.
    let (tx, rx) = tokio::sync::mpsc::channel::<RawPtr>(1);

    Box::into_raw(Box::new(RequestChannel {
        _sender: Some(tx),
        _receiver: Some(rx),
    }))
}

#[unsafe(no_mangle)]
pub extern "C" fn request_channel_free(ptr: *mut RequestChannel) {
    if !ptr.is_null() {
        unsafe { drop(Box::from_raw(ptr)) };
    }
}

/**
 * Send a message through a RPC. The value should be a stable reference to a Message.
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_send(ptr: *mut RequestChannel, value: *mut c_void) {
    unsafe {
        if let Some(channel) = ptr.as_mut() {
            if let Some(sender) = channel._sender.as_ref() {
                sender.blocking_send(RawPtr(value)).unwrap();
            }
        }
    }
}

/**
 * Signal that the client will write no more requests
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_signal_end(ptr: *mut RequestChannel) {
    unsafe {
        if let Some(channel) = ptr.as_mut() {
            channel.close_sender()
        }
    }
}

/**
 * Construct a new metadata object. Expects a string array where the key and value are always next to each other.
 * The array must be null terminated.
 */
#[unsafe(no_mangle)]
pub extern "C" fn metadata_create(metadata: *const *const c_char) -> *mut RustMetadata {
    let mut map = MetadataMap::new();

    unsafe {
        let mut i = 0;
        while !(*metadata.add(i)).is_null() && !(*metadata.add(i + 1)).is_null() {
            let key = match CStr::from_ptr(*metadata.add(i)).to_str() {
                Ok(str) => str,
                Err(_) => {
                    continue;
                }
            };

            let value = match CStr::from_ptr(*metadata.add(i + 1)).to_str() {
                Ok(str) => str,
                Err(_) => {
                    continue;
                }
            };

            if let Ok(key) = MetadataKey::from_str(key) {
                if let Ok(value) = MetadataValue::from_str(value) {
                    map.insert(key, value);
                }
            }

            i += 2;
        }
    }

    Box::into_raw(Box::new(RustMetadata { _metadata: map }))
}

/**
 * Iterate over all elements in the native metadata, calling block on each key-value pair.
 */
#[unsafe(no_mangle)]
pub extern "C" fn metadata_iterate(
    metadata: *mut RustMetadata,
    data: *mut c_void,
    block: extern "C" fn(data: *mut c_void, key: *const c_char, value: *const c_char),
) {
    if metadata.is_null() {
        return;
    }
    unsafe {
        if let Some(metadata) = metadata.as_mut() {
            for (key, value) in metadata._metadata.as_ref() {
                let key_string = CString::new(key.as_str()).unwrap();
                let value_string = CString::new(value.to_str().unwrap()).unwrap();

                block(data, key_string.into_raw(), value_string.into_raw());
            }
        }
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn metadata_free(metadata: *mut RustMetadata) {
    if metadata.is_null() {
        return;
    }
    unsafe {
        drop(Box::from_raw(metadata));
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn string_free(s: *mut c_char) {
    if s.is_null() {
        return;
    }
    unsafe {
        drop(CString::from_raw(s));
    }
}

/**
 * Stops the associated task and frees the pointer.
 */
#[unsafe(no_mangle)]
pub extern "C" fn rpc_task_abort(task: *mut RpcTask) {
    if task.is_null() {
        return;
    }

    unsafe {
        let mut t = Box::from_raw(task);
        t.as_mut()._handle.abort();

        drop(t)
    }
}
