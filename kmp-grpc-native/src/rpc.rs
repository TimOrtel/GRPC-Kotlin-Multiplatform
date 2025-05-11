use crate::cinterop::*;
use crate::rawcoding::{DecodeMessage, EncodeMessage, RawBytesCodec, RpcOnMessageWrittenInternal};
use crate::rawpointer::RawPtr;
use crate::rpc::RpcFinalization::Trailers;
use crate::rpc::RpcResult::{Cancelled, WithResult};
use env_logger::Target;
use log::{LevelFilter, trace};
use once_cell::sync::Lazy;
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_void};
use std::ptr::null_mut;
use tokio::runtime::Runtime;
use tokio::sync::mpsc::Receiver;
use tokio::sync::watch;
use tokio::task::JoinHandle;
use tonic::client::Grpc;
use tonic::codegen::http::uri::PathAndQuery;
use tonic::codegen::tokio_stream::StreamExt;
use tonic::codegen::tokio_stream::wrappers::ReceiverStream;
use tonic::metadata::{KeyRef, MetadataMap};
use tonic::transport::Channel;
use tonic::{Code, IntoStreamingRequest, Request, Response, Status, Streaming};

pub static TOKIO_RT: Lazy<Runtime> = Lazy::new(|| {
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

#[unsafe(no_mangle)]
pub extern "C" fn init(enable_trace_logs: bool) {
    env_logger::Builder::new()
        .filter_level(if enable_trace_logs {
            LevelFilter::Trace
        } else {
            LevelFilter::Info
        })
        .target(Target::Stdout)
        .init();
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
    trace!("rpc_implementation()");

    let (shutdown_tx, mut shutdown_rx) = watch::channel(false);

    let on_done = move |user_data: RawPtr,
                        status: i32,
                        message: *const c_char,
                        metadata: *mut RustMetadata,
                        trailers: *mut RustMetadata| {
        trace!("rpc_implementation() - on_done()");

        on_done(user_data.0, status, message, metadata, trailers)
    };

    let on_message_received = move |user_data: RawPtr, message: RawPtr| {
        trace!("rpc_implementation() - on_message_received()");

        on_message_received(user_data.0, message.0);
    };

    let on_initial_metadata_received = move |user_data: RawPtr, metadata: *mut RustMetadata| {
        trace!("rpc_implementation() - on_initial_metadata_received()");

        on_initial_metadata_received(user_data.0, metadata)
    };

    let on_done_error = move |user_data: RawPtr, err_str: &str| {
        trace!("rpc_implementation() - on_done_error()");

        on_done(
            user_data,
            i32::from(Code::Internal),
            CString::new(err_str).unwrap().into_raw(),
            null_mut(),
            null_mut(),
        );
    };

    // These must be closed in Kotlin code
    let user_data = RawPtr(user_data);
    let channel = unsafe { channel.as_mut() };

    // These will be closed by rust
    let request_receiver: Receiver<RawPtr> = unsafe {
        match request_channel.as_mut().and_then(|rc| rc._receiver.take()) {
            Some(rc) => rc,
            None => {
                on_done_error(user_data, "no request channel provided");
                return null_mut();
            }
        }
    };
    let metadata = unsafe { Box::from_raw(metadata) };

    let path = unsafe {
        match CStr::from_ptr(path)
            .to_str()
            .map_err(|e| e.to_string().as_str())
            .and_then(|s| PathAndQuery::from_maybe_shared(s).map_err(|e| e.to_string().as_str()))
        {
            Ok(path_and_query) => path_and_query,
            Err(err) => {
                on_done_error(user_data, err);
                return null_mut();
            }
        }
    };

    let codec = RawBytesCodec {
        user_data,
        encode: serialize_request,
        decode: deserialize_response,
        on_message_written: RpcOnMessageWrittenInternal {
            on_message_written,
            user_data,
        },
    };

    let native_channel = match channel.and_then(|c| c._channel.clone()) {
        Some(c) => { c }
        None => {
            on_done_error(user_data, "no channel provided");
            return null_mut();
        }
    };

    let handle = create_rpc_future(
        codec,
        shutdown_rx,
        on_done,
        on_message_received,
        on_initial_metadata_received,
        on_done_error,
        user_data,
        native_channel,
        request_receiver,
        metadata,
        path
    );

    trace!("rpc_implementation() - returning with handle");

    Box::into_raw(Box::new(RpcTask {
        _handle: handle,
        _sender: shutdown_tx,
    }))
}

unsafe fn create_rpc_future(
    codec: RawBytesCodec,
    shutdown_rx: watch::Receiver<bool>,
    on_done: fn(RawPtr, i32, *const c_char, *mut RustMetadata, *mut RustMetadata),
    on_message_received: fn(RawPtr, RawPtr),
    on_initial_metadata_received: fn(RawPtr, *mut RustMetadata),
    on_done_error: fn(RawPtr, &str),
    user_data: RawPtr,
    channel: Channel,
    request_receiver: Receiver<RawPtr>,
    metadata: Box<RustMetadata>,
    path: PathAndQuery,
) -> JoinHandle<()> {
    TOKIO_RT.spawn(async move {
        trace!("rpc_implementation() - enter future");

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

        let on_done_with_result = move |result: RpcFinalization| match result {
            Trailers(trailers) => {
                on_done_impl(-1, "", MetadataMap::new(), trailers);
            }
            RpcFinalization::Status(status) => on_done_impl(
                i32::from(status.code()),
                status.message(),
                status.metadata().clone(),
                None,
            ),
        };

        let mut request_stream: Request<ReceiverStream<RawPtr>> =
            ReceiverStream::new(request_receiver).into_streaming_request();

        insert_custom_metadata(metadata, request_stream.metadata_mut());

        let call_result = perform_and_handle_call(
            codec,
            user_data,
            on_message_received,
            on_initial_metadata_received,
            channel,
            request_stream,
            path,
            shutdown_rx,
        )
            .await;

        match call_result {
            Ok(rpc_result) => match rpc_result {
                WithResult(result) => {
                    on_done_with_result(result);
                }
                Cancelled(reason) => {
                    on_done_with_result(RpcFinalization::Status(Status::cancelled(reason)));
                }
            },
            Err(err) => {
                on_done_error(user_data, err.as_str());
            }
        }
    })
}

async unsafe fn perform_and_handle_call(
    codec: RawBytesCodec,
    user_data: RawPtr,
    on_message_received: fn(RawPtr, RawPtr),
    on_initial_metadata_received: fn(RawPtr, *mut RustMetadata),
    channel: Channel,
    request_stream: Request<ReceiverStream<RawPtr>>,
    path: PathAndQuery,
    mut shutdown_rx: watch::Receiver<bool>,
) -> Result<RpcResult<RpcFinalization>, String> {
    let start_streaming_result: Result<RpcResult<Response<Streaming<RawPtr>>>, String> = tokio::select! {
        r = async {
            match build_and_wait_for_grpc(channel).await {
                Ok(mut grpc) => {
                    grpc.streaming(request_stream, path, codec)
                        .await
                        .map_err(|err| err.to_string())
                }
                Err(err) => { Err(err) }
            }
        } => {
            trace!("rpc_implementation() - grpc.streaming returned");
            r.map(|response| { WithResult(response)})
        }

        _ = async { shutdown_rx.changed().await } => {
            trace!("rpc_implementation() - shutdown_rx.changed() returned. Stop waiting for grpc.streaming");
            Ok(Cancelled("rpc cancelled while waiting for grpc.streaming".to_string()))
        }
    };

    match start_streaming_result {
        Ok(rpc_result) => match rpc_result {
            WithResult(response) => {
                let (metadata, body, _) = response.into_parts();

                on_initial_metadata_received(user_data, Box::into_raw(Box::new(RustMetadata {
                    _metadata: metadata,
                })));

                Ok(read_response_body(user_data, body, shutdown_rx, on_message_received).await)
            }
            Cancelled(message) => {
                trace!("rpc_implementation() - grpc.streaming returned CANCELLED");
                Ok(Cancelled(message))
            }
        },
        Err(str) => Err(str),
    }
}

async fn build_and_wait_for_grpc(
    channel: Channel,
) -> Result<Grpc<Channel>, String> {
    let mut grpc = Grpc::new(channel);

    match grpc.ready().await {
        Ok(_) => Ok(grpc),
        Err(err) => {
            trace!("rpc_implementation() - grpc.ready() failed");
            Err(err.to_string())
        }
    }
}

/**
 * Reads the response body until either the reading completed or the shutdown event is sent.
 */
async fn read_response_body(
    user_data: RawPtr,
    mut body: Streaming<RawPtr>,
    mut shutdown_rx: watch::Receiver<bool>,
    on_message_received: fn(RawPtr, RawPtr),
) -> RpcResult<RpcFinalization> {
    trace!("rpc_implementation() - starting to read body");

    tokio::select! {
        rpc_result = async {
            loop {
                match body.next().await {
                    Some(result) => match result {
                        Ok(message) => {
                            trace!("rpc_implementation() - body.next() returned message");

                            on_message_received(user_data, message);
                        }
                        Err(status) => {
                            trace!("rpc_implementation() - body.next() returned status error");

                            return WithResult(RpcFinalization::Status(status));
                        }
                    },
                    None => {
                        trace!("rpc_implementation() - body.next() returned None");

                        break;
                    }
                }
            }

            trace!("rpc_implementation() - finalizing rpc");

            match body.trailers().await {
                Ok(trailers) => WithResult(Trailers(trailers)),
                Err(status) => { WithResult(RpcFinalization::Status(status)) }
            }
        } => {
            trace!("rpc_implementation() - body.trailers() returned");
            drop(body);

            rpc_result
        }

        _ = async { shutdown_rx.changed().await } => {
            trace!("rpc_implementation() - shutdown_rx.changed() returned. Stop waiting for body/trailers reading.");

            drop(body);

            Cancelled("rpc cancelled while waiting for grpc.streaming".to_string())
        }
    }
}

fn insert_custom_metadata(custom_metadata: Box<RustMetadata>, target: &mut MetadataMap) {
    trace!("insert_custom_metadata() - start");

    let md = custom_metadata._metadata;

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

    trace!("insert_custom_metadata() - end");
}

enum RpcResult<R> {
    WithResult(R),
    Cancelled(String),
}

enum RpcFinalization {
    Trailers(Option<MetadataMap>),
    Status(Status),
}
