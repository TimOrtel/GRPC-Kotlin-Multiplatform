mod rawcoding;
mod rawpointer;

use crate::rawcoding::{DecodeMessage, EncodeMessage, RawBytesCodec};
use crate::rawpointer::RawPtr;
use once_cell::sync::Lazy;
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_void};
use tokio::runtime::Runtime;
use tokio::sync::mpsc::Receiver;
use tokio::sync::mpsc::Sender;
use tonic::client::Grpc;
use tonic::codegen::http::uri::PathAndQuery;
use tonic::codegen::tokio_stream::StreamExt;
use tonic::codegen::tokio_stream::wrappers::ReceiverStream;
use tonic::transport::{Channel, Error};
use tonic::{Code, IntoStreamingRequest, Request, Status};

static TOKIO_RT: Lazy<Runtime> = Lazy::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .unwrap()
});

type RpcCallback =
extern "C" fn(user_data: *mut c_void, status_code: i32, status_message: *const c_char);
type RpcOnMessageReceived = extern "C" fn(user_data: *mut c_void, message: *mut c_void);

pub struct RequestChannel {
    _sender: Option<Sender<RawPtr>>,
    _receiver: Option<Receiver<RawPtr>>,
}

impl RequestChannel {
    pub fn close_sender(&mut self) {
        self._sender.take();
    }
}

#[unsafe(no_mangle)]
pub unsafe extern "C" fn rpc_implementation(
    host: *const c_char,
    path: *const c_char,
    request_channel: *mut RequestChannel,
    user_data: *mut c_void,
    serialize_request: EncodeMessage,
    deserialize_response: DecodeMessage,
    on_message_received: RpcOnMessageReceived,
    on_done: RpcCallback,
) {
    println!("rpc_implementation");

    let host = unsafe { CStr::from_ptr(host).to_str().unwrap().to_string() };
    let path = unsafe { CStr::from_ptr(path).to_str().unwrap().to_string() };

    let on_done = move |user_data: RawPtr, status: i32, message: *const c_char| {
        on_done(user_data.0, status, message)
    };

    let on_message_received = move |user_data: RawPtr, message: RawPtr| {
        on_message_received(user_data.0, message.0);
    };

    let user_data = RawPtr(user_data);
    let request_channel = unsafe { request_channel.as_mut() };

    println!(
        "rpc_implementation - spawning with host={} and path={}",
        host, path
    );
    TOKIO_RT.spawn(async move {
        let on_done_error = move |err: Error| {
            on_done(
                user_data,
                i32::from(Code::Internal),
                CString::new(err.to_string()).unwrap().into_raw(),
            );
        };

        let on_done_status = move |status: Status| {
            on_done(
                user_data,
                i32::from(status.code()),
                CString::new(status.message()).unwrap().into_raw(),
            );
        };

        println!(
            "rpc_implementation - spawned with host={} and path={}",
            host, path
        );
        let channel = match Channel::from_shared(host).unwrap().connect().await {
            Ok(channel) => channel,
            Err(err) => {
                on_done_error(err);
                return;
            }
        };

        println!("rpc_implementation - create path = {}", path);
        let path = PathAndQuery::from_maybe_shared(path).unwrap();

        let mut grpc = Grpc::new(channel);
        if let Err(err) = grpc.ready().await {
            on_done_error(err);
        }

        println!("rpc_implementation - call");

        let request_stream: Request<ReceiverStream<RawPtr>> = if let Some(channel) = request_channel {
            if let Some(receiver) = channel._receiver.take() {
                ReceiverStream::new(receiver).into_streaming_request()
            } else {
                on_done_status(Status::new(Code::Internal, "no receiver channel"));
                return;
            }
        } else {
            on_done_status(Status::new(Code::Internal, "no request channel"));
            return;
        };

        println!("rpc_implementation - receive request stream");

        let result = grpc
            .streaming(
                request_stream,
                path,
                RawBytesCodec {
                    encode: serialize_request,
                    decode: deserialize_response,
                },
            )
            .await;

        println!("rpc_implementation - result");

        match result {
            Ok(response) => {
                let mut stream = response.into_inner();
                loop {
                    match stream.next().await {
                        Some(result) => match result {
                            Ok(message) => {
                                on_message_received(user_data, message);
                            }
                            Err(status) => {
                                on_done_status(status);
                                return;
                            }
                        },
                        None => {
                            println!("rpc_implementation - server stream done");
                            break;
                        }
                    }
                }
            }
            Err(status) => {
                println!("rpc_implementation - error: {}", status);

                on_done_status(status);
            }
        }

        println!("rpc_implementation - done");
    });

    println!("rpc_implementation - returning");
}

#[unsafe(no_mangle)]
pub extern "C" fn request_channel_create() -> *mut RequestChannel {
    let (tx, rx) = tokio::sync::mpsc::channel::<RawPtr>(8);

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

#[unsafe(no_mangle)]
pub extern "C" fn request_channel_signal_end(ptr: *mut RequestChannel) {
    unsafe {
        if let Some(channel) = ptr.as_mut() {
            channel.close_sender()
        }
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
