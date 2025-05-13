use crate::rawpointer::RawPtr;
use crate::rpc::TOKIO_RT;
use log::trace;
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_void};
use std::ptr::null_mut;
use std::str::FromStr;
use tokio::sync::mpsc::{Receiver, Sender};
use tokio::sync::mpsc::error::TrySendError;
use tokio::sync::watch;
use tonic::metadata::{MetadataKey, MetadataMap, MetadataValue};
use tonic::transport::Channel;

/**
 * Holds the channel used to send messages into the RPC.
 */
pub struct RequestChannel {
    _sender: Option<Sender<RawPtr>>,
    pub _receiver: Option<Receiver<RawPtr>>,
}

/**
 * Holds the metadata of Rust.
 */
pub struct RustMetadata {
    pub _metadata: MetadataMap,
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
    pub _channel: Option<Channel>,
}

/**
 * Holds a handle to a task on which the rpc is running. Can be used to abort the task.
 */
pub struct RpcTask {
    pub _handle: tokio::task::JoinHandle<()>,
    pub _sender: watch::Sender<bool>,
}

/**
 * Create a new channel from the given host. Returns null if the channel could not be created.
 */
#[unsafe(no_mangle)]
pub extern "C" fn channel_create(host: *const c_char) -> *mut RustChannel {
    trace!("channel_create()");

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
            _channel: Some(endpoint.connect_lazy()),
        })),
        Err(_) => null_mut(),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn channel_free(channel: *mut RustChannel) {
    trace!("channel_free() with: {:p}", channel);

    if channel.is_null() {
        return;
    }
    unsafe {
        if let Some(channel) = channel.as_mut() {
            channel._channel.take();
        }

        drop(Box::from_raw(channel));
    }
}

/**
 * Create a new request channel which allows to send messages into a RPC
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_create() -> *mut RequestChannel {
    trace!("request_channel_create()");

    // A buffer of 1 is enough, as the Kotlin implementation always waits for the previous message to be written to the wire first.
    let (tx, rx) = tokio::sync::mpsc::channel::<RawPtr>(1);

    Box::into_raw(Box::new(RequestChannel {
        _sender: Some(tx),
        _receiver: Some(rx),
    }))
}

#[unsafe(no_mangle)]
pub extern "C" fn request_channel_free(ptr: *mut RequestChannel) {
    trace!("request_channel_free() with channel: {:p}", ptr);

    if !ptr.is_null() {
        unsafe { drop(Box::from_raw(ptr)) };
    }
}

#[repr(C)]
#[derive(Debug, Copy, Clone)]
pub enum RequestChannelResult {
    Ok = 0,
    Full = 1,
    Closed = 2,
    NoSender = 3,
}

/**
 * Send a message through a RPC. The value should be a stable reference to a Message.
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_send(ptr: *mut RequestChannel, value: *mut c_void) -> RequestChannelResult {
    trace!(
        "request_channel_send() with ptr: {:p}, value: {:p}",
        ptr, value
    );

    unsafe {
        match ptr.as_mut().and_then(|rc| rc._sender.as_ref()) {
            Some(sender) => {
                trace!("request_channel_send() - has sender");
                match sender.try_send(RawPtr(value)) {
                    Ok(_) => RequestChannelResult::Ok,
                    Err(err) => {
                        match err {
                            TrySendError::Full(_) => {
                                trace!("request_channel_send() - could not send message -> full");
                                RequestChannelResult::Full
                            }
                            TrySendError::Closed(_) => {
                                trace!("request_channel_send() - could not send message -> closed");
                                RequestChannelResult::Closed
                            }
                        }
                    }
                }
            }
            None => {
                trace!("request_channel_send() - no sender");
                RequestChannelResult::NoSender
            }
        }
    }
}

/**
 * Signal that the client will write no more requests
 */
#[unsafe(no_mangle)]
pub extern "C" fn request_channel_signal_end(ptr: *mut RequestChannel) {
    trace!("request_channel_signal_end() with ptr: {:p}", ptr);

    unsafe {
        if let Some(channel) = ptr.as_mut() {
            trace!("request_channel_signal_end() - has channel");

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
    trace!("metadata_create()");

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

    trace!("metadata_create() - returning");

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
    trace!(
        "metadata_iterate() with metadata: {:p}, data: {:p}",
        metadata, data
    );

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
    trace!("metadata_free() with: {:p}", metadata);

    if metadata.is_null() {
        return;
    }
    unsafe {
        drop(Box::from_raw(metadata));
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn string_free(s: *mut c_char) {
    trace!("string_free() with: {:p}", s);

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
    trace!("rpc_task_abort() with: {:p}", task);

    if task.is_null() {
        return;
    }

    unsafe {
        let mut task_box = Box::from_raw(task);

        match task_box.as_mut()._sender.send(true) {
            Ok(_) => {}
            Err(_) => {
                trace!("rpc_task_abort() - failed to send shutdown signal");
            }
        }

        trace!("rpc_task_abort() - aborted task");

        drop(task_box)
    }
}
