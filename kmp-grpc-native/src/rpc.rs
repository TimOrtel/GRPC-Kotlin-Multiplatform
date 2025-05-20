use crate::cinterop::*;
use crate::rawcoding::{DecodeMessage, EncodeMessage, RawBytesCodec};
use crate::rawpointer::RawPtr;
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
use tonic::client::{Grpc};
use tonic::codegen::http::uri::PathAndQuery;
use tonic::codegen::tokio_stream::StreamExt;
use tonic::codegen::tokio_stream::wrappers::ReceiverStream;
use tonic::metadata::{KeyRef, MetadataMap};
use tonic::transport::Channel;
use tonic::{Code, IntoStreamingRequest, Request, Response, Status, Streaming};

/// cbindgen:ignore
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
    on_initial_metadata_received: RpcOnInitialMetadataReceived,
    on_done: RpcOnDone,
) -> *mut RpcTask {
    trace!("rpc_implementation()");

    let (shutdown_tx, shutdown_rx) = watch::channel(false);

    let on_done_error: fn(RawPtr, &str, RpcOnDone) = |user_data: RawPtr, err_str: &str, on_done: RpcOnDone| {
        trace!("rpc_implementation() - on_done_error()");

        on_done(
            user_data.0,
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
                on_done_error(user_data, "no request channel provided", on_done);
                return null_mut();
            }
        }
    };
    let metadata = unsafe { Box::from_raw(metadata) };

    let path = unsafe {
        match CStr::from_ptr(path)
            .to_str()
            .map_err(|e| e.to_string().to_string())
            .and_then(|s| PathAndQuery::from_maybe_shared(s).map_err(|e| e.to_string().to_string()))
        {
            Ok(path_and_query) => path_and_query,
            Err(err) => {
                on_done_error(user_data, err.as_str(), on_done);
                return null_mut();
            }
        }
    };

    let codec = RawBytesCodec {
        user_data,
        encode: serialize_request,
        decode: deserialize_response,
    };

    let native_channel = match channel.map(|c| c._channel.clone()) {
        Some(c) => { c }
        None => {
            on_done_error(user_data, "no channel provided", on_done);
            return null_mut();
        }
    };

    let handle = unsafe {
        create_rpc_future(
            codec,
            shutdown_rx,
            on_done,
            on_message_received,
            on_initial_metadata_received,
            user_data,
            native_channel,
            request_receiver,
            metadata,
            path,
        )
    };

    trace!("rpc_implementation() - returning with handle");

    Box::into_raw(Box::new(RpcTask {
        _handle: handle,
        _sender: shutdown_tx,
    }))
}

unsafe fn create_rpc_future(
    codec: RawBytesCodec,
    mut shutdown_rx: watch::Receiver<bool>,
    on_done: RpcOnDone,
    on_message_received: RpcOnMessageReceived,
    on_initial_metadata_received: RpcOnInitialMetadataReceived,
    user_data: RawPtr,
    channel: Channel,
    request_receiver: Receiver<RawPtr>,
    metadata: Box<RustMetadata>,
    path: PathAndQuery,
) -> JoinHandle<()> {
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

        let on_done_with_status = move |status: Status| {
            on_done_impl(
                i32::from(status.code()),
                status.message(),
                status.metadata().clone(),
                None,
            );
        };

        let on_done_with_trailers = move |trailers: Option<MetadataMap>| {
            on_done_impl(-1, "", MetadataMap::new(), trailers);
        };

        let on_done_cancelled = |reason: String| {
            on_done_with_status(Status::cancelled(reason))
        };

        let on_done_error = |message: String| {
            on_done_impl(
                i32::from(Code::Internal),
                message.as_str(),
                MetadataMap::new(),
                None,
            );
        };

        let mut request_stream: Request<ReceiverStream<RawPtr>> =
            ReceiverStream::new(request_receiver).into_streaming_request();

        insert_custom_metadata(metadata, request_stream.metadata_mut());

        // Start request

        let start_streaming_result: Result<RpcResult<Response<Streaming<RawPtr>>>, String> = tokio::select! {
            r = async {
                match build_and_wait_for_grpc(channel).await {
                    Ok(mut grpc) => {
                        match grpc.streaming(request_stream, path, codec)
                            .await {
                            Ok(response) => Ok(WithResult(response)),
                            Err(status) => Ok(RpcResult::Status(status))
                        }

                    }
                    Err(err) => { Err(err) }
                }
            } => {
                trace!("rpc_implementation() - grpc.streaming returned");
                r
            }

            _ = async { shutdown_rx.changed().await } => {
                trace!("rpc_implementation() - shutdown_rx.changed() returned. Stop waiting for grpc.streaming");

                Ok(Cancelled("rpc cancelled while waiting for grpc.streaming".to_string()))
            }
        };

        let mut body = match start_streaming_result {
            Ok(rpc_result) => match rpc_result {
                WithResult(response) => {
                    let (metadata, body, _) = response.into_parts();

                    on_initial_metadata_received(user_data, Box::into_raw(Box::new(RustMetadata {
                        _metadata: metadata,
                    })));

                    body
                }
                RpcResult::Status(status) => {
                    trace!("rpc_implementation() - grpc.streaming returned STATUS with code {}", i32::from(status.code()));
                    on_done_with_status(status);
                    return;
                }
                Cancelled(message) => {
                    trace!("rpc_implementation() - grpc.streaming returned CANCELLED");
                    on_done_cancelled(message);
                    return;
                }
            },
            Err(str) => {
                trace!("rpc_implementation() - grpc.streaming returned error");

                on_done_error(str);
                return;
            }
        };

        // Read rpc body

        let call_result: RpcResult<Option<MetadataMap>> = tokio::select! {
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

                                return RpcResult::Status(status);
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
                    Ok(trailers) => WithResult(trailers),
                    Err(status) => { RpcResult::Status(status) }
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
        };

        // Finalization

        match call_result {
            WithResult(result) => {
                on_done_with_trailers(result);
            }
            RpcResult::Status(status) => {
                on_done_with_status(status);
            }
            Cancelled(reason) => {
                on_done_cancelled(reason);
            }
        }
    })
}

async fn build_and_wait_for_grpc(
    channel: Channel,
) -> Result<Grpc<Channel>, String> {
    trace!("build_and_wait_for_grpc()");
    let mut grpc = Grpc::new(channel);
    trace!("build_and_wait_for_grpc() - grpc.new() returned");
    
    match grpc.ready().await {
        Ok(_) => Ok(grpc),
        Err(err) => {
            trace!("build_and_wait_for_grpc() - grpc.ready() failed");
            Err(err.to_string())
        }
    }
}

fn insert_custom_metadata(custom_metadata: Box<RustMetadata>, target: &mut MetadataMap) {
    trace!("insert_custom_metadata() - start");

    let md = custom_metadata._metadata;

    for key in md.keys() {
        match key {
            KeyRef::Ascii(ascii_key) => {
                for value in md.get_all(ascii_key) {
                    target.append(ascii_key, value.clone());
                }
            }
            KeyRef::Binary(bin_key) => {
                for value in md.get_all_bin(bin_key) {
                    target.append_bin(bin_key, value.clone());
                }
            }
        }
    }

    trace!("insert_custom_metadata() - end");
}

enum RpcResult<R> {
    WithResult(R),
    Status(Status),
    Cancelled(String),
}
