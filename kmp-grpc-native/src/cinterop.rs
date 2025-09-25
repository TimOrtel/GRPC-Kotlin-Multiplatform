use crate::rawpointer::RawPtr;
use crate::rpc::TOKIO_RT;
use log::trace;
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_void};
use std::ptr::null_mut;
use std::str::FromStr;
use tokio::sync::mpsc::error::TrySendError;
use tokio::sync::mpsc::{Receiver, Sender};
use tokio::sync::watch;
use tonic::metadata::{
    AsciiMetadataKey, AsciiMetadataValue, BinaryMetadataKey, BinaryMetadataValue, KeyRef,
    MetadataMap,
};
use tonic::transport::{Channel, ClientTlsConfig};

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
    pub _channel: Channel,
}

/**
 * Holds a handle to a task on which the rpc is running. Can be used to abort the task.
 */
pub struct RpcTask {
    pub _handle: tokio::task::JoinHandle<()>,
    pub _sender: watch::Sender<bool>,
}

/**
 * Create a new channel from the given host with optional keepalive settings. Returns null if the channel could not be created.
 */
#[unsafe(no_mangle)]
pub extern "C" fn channel_create(
    host: *const c_char,
    use_plaintext: bool,
    keepalive_time_nanos: u64,
    keepalive_timeout_nanos: u64,
    keepalive_without_calls: bool
) -> *mut RustChannel {
    trace!("channel_create()");

    let _guard = TOKIO_RT.enter();

    let host = unsafe {
        match CStr::from_ptr(host).to_str() {
            Ok(str) => str.to_string(),
            Err(_) => {
                return null_mut();
            }
        }
    };

    match Channel::from_shared(host)
        .ok()
        .and_then(|x| {
            if use_plaintext {
                Some(x)
            } else {
                x.tls_config(ClientTlsConfig::new().with_webpki_roots()).ok()
            }
        })
    {
        Some(endpoint) => {
            Box::into_raw(Box::new(RustChannel {
                _channel: endpoint
                    .keep_alive_timeout(std::time::Duration::from_nanos(keepalive_timeout_nanos))
                    .http2_keep_alive_interval(std::time::Duration::from_nanos(keepalive_time_nanos))
                    .keep_alive_while_idle(keepalive_without_calls)
                    .connect_lazy(),
            }))
        }
        None => null_mut(),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn channel_free(channel: *mut RustChannel) {
    trace!("channel_free() with: {:p}", channel);

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
pub extern "C" fn request_channel_send(
    ptr: *mut RequestChannel,
    value: *mut c_void,
) -> RequestChannelResult {
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
                    Err(err) => match err {
                        TrySendError::Full(_) => {
                            trace!("request_channel_send() - could not send message -> full");
                            RequestChannelResult::Full
                        }
                        TrySendError::Closed(_) => {
                            trace!("request_channel_send() - could not send message -> closed");
                            RequestChannelResult::Closed
                        }
                    },
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
 * Construct a new metadata object.
 * For the ascii pairs: Expects a string array where the key and value are always next to each other. The array must be null terminated.
 * For the binary pairs: binary_keys is a null terminated array of the key names. binary_ptrs is not null terminated but must have the same size as binary_keys.
 * binary_sizes must have the same size as binary_ptrs and give the number of bytes in each byte array of binary_ptrs.
 */
#[unsafe(no_mangle)]
pub extern "C" fn metadata_create(
    ascii_entries: *const *const c_char,
    binary_keys: *const *const c_char,
    binary_ptrs: *const *const u8,
    binary_sizes: *const usize,
) -> *mut RustMetadata {
    trace!("metadata_create()");

    let mut map = MetadataMap::new();

    // Ascii values
    unsafe {
        let mut i = 0;
        while !(*ascii_entries.add(i)).is_null() && !(*ascii_entries.add(i + 1)).is_null() {
            let key = match CStr::from_ptr(*ascii_entries.add(i)).to_str() {
                Ok(str) => str,
                Err(_) => {
                    continue;
                }
            };

            let value = match CStr::from_ptr(*ascii_entries.add(i + 1)).to_str() {
                Ok(str) => str,
                Err(_) => {
                    continue;
                }
            };

            if let Ok(ascii_key) = AsciiMetadataKey::from_str(key) {
                if let Ok(ascii_value) = AsciiMetadataValue::from_str(value) {
                    map.append(ascii_key.clone(), ascii_value);
                }
            }

            i += 2;
        }
    }

    // Binary values
    unsafe {
        if !binary_keys.is_null() && !binary_ptrs.is_null() && !binary_sizes.is_null() {
            let mut j = 0;
            while !(*binary_keys.add(j)).is_null() {
                let key_cstr = CStr::from_ptr(*binary_keys.add(j));
                let key = match key_cstr.to_str() {
                    Ok(k) => k,
                    Err(_) => {
                        j += 1;
                        continue;
                    }
                };

                if let Ok(key) = BinaryMetadataKey::from_str(key) {
                    let data_ptr = *binary_ptrs.add(j);
                    let size = *binary_sizes.add(j);

                    if !data_ptr.is_null() {
                        let data_slice = std::slice::from_raw_parts(data_ptr, size);
                        map.append_bin(key, BinaryMetadataValue::from_bytes(data_slice));
                    }
                }

                j += 1;
            }
        }
    }

    trace!("metadata_create() - returning");

    Box::into_raw(Box::new(RustMetadata { _metadata: map }))
}

/**
 * Iterate over all elements in the native metadata, calling block_ascii or block_binary for each entry.
 */
#[unsafe(no_mangle)]
pub extern "C" fn metadata_iterate(
    metadata: *mut RustMetadata,
    data: *mut c_void,
    block_ascii: extern "C" fn(data: *mut c_void, key: *const c_char, value: *const c_char),
    block_binary: extern "C" fn(data: *mut c_void, key: *const c_char, ptr: *const u8, size: usize),
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
            metadata._metadata.keys().for_each(|key| {
                let key_str = match key {
                    KeyRef::Ascii(ak) => ak.as_str(),
                    KeyRef::Binary(bk) => bk.as_str(),
                };

                let key_string = CString::new(key_str).unwrap();

                match key {
                    KeyRef::Ascii(ascii_key) => {
                        for value in metadata._metadata.get_all(ascii_key) {
                            let value_string = CString::new(value.to_str().unwrap()).unwrap();

                            block_ascii(
                                data,
                                key_string.clone().into_raw(),
                                value_string.into_raw(),
                            );
                        }
                    }
                    KeyRef::Binary(bin_key) => {
                        for value in metadata._metadata.get_all_bin(bin_key) {
                            match value.to_bytes() {
                                Ok(bytes) => {
                                    block_binary(
                                        data,
                                        key_string.clone().into_raw(),
                                        bytes.as_ptr(),
                                        bytes.len(),
                                    );
                                }
                                Err(_) => {}
                            }
                        }
                    }
                }
            });
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
