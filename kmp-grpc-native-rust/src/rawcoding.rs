use crate::rawpointer::RawPtr;
use bytes::{Buf, BufMut};
use std::os::raw::c_void;
use std::slice::from_raw_parts;
use tonic::codec::{DecodeBuf, EncodeBuf};
use tonic::{
    Status,
    codec::{Codec, Decoder, Encoder},
};
use crate::RpcOnMessageWritten;

pub type FreeCByteArray = extern "C" fn(*mut c_void);

pub struct CByteArray {
    data: RawPtr,
    ptr: *const u8,
    len: usize,
    free: FreeCByteArray,
}

pub type EncodeMessage = extern "C" fn(*mut c_void) -> *mut CByteArray;
pub type DecodeMessage = extern "C" fn(*mut c_void, ptr: *const u8, len: usize) -> *mut c_void;

pub struct RpcOnMessageWrittenInternal {
    pub(crate) on_message_written: RpcOnMessageWritten,
    pub(crate) user_data: RawPtr,
}

impl RpcOnMessageWrittenInternal {
    fn call(&self) {
        (self.on_message_written)(self.user_data.0);
    }
}

impl Clone for RpcOnMessageWrittenInternal {
    fn clone(&self) -> Self {
        RpcOnMessageWrittenInternal {
            on_message_written: self.on_message_written,
            user_data: self.user_data,
        }
    }
}

#[derive(Clone)]
pub(crate) struct RawBytesCodec {
    pub(crate) encode: EncodeMessage,
    pub(crate) decode: DecodeMessage,
    pub(crate) on_message_written: RpcOnMessageWrittenInternal,
    pub(crate) user_data: RawPtr,
}

impl Codec for RawBytesCodec {
    type Encode = RawPtr;
    type Decode = RawPtr;

    type Encoder = RawEncoder;
    type Decoder = RawDecoder;

    fn encoder(&mut self) -> Self::Encoder {
        RawEncoder {
            encode_message: self.encode,
            on_message_written: self.on_message_written.clone()
        }
    }

    fn decoder(&mut self) -> Self::Decoder {
        RawDecoder(self.user_data, self.decode)
    }
}

pub(crate) struct RawEncoder {
    pub(crate) encode_message: EncodeMessage,
    pub(crate) on_message_written: RpcOnMessageWrittenInternal
}

impl Encoder for RawEncoder {
    type Item = RawPtr;
    type Error = Status;

    fn encode(&mut self, item: Self::Item, dst: &mut EncodeBuf<'_>) -> Result<(), Self::Error> {
        println!("encode");
        let ba = unsafe { Box::from_raw((self.encode_message)(item.0)) };
        println!("encode - have byte array {}", ba.len);

        if ba.len == 0 {
            self.on_message_written.call();
            return Ok(());
        }

        unsafe {
            dst.put_slice(from_raw_parts(ba.ptr, ba.len));
        }

        println!("encode - put slice");

        println!("encode - freeing");

        self.on_message_written.call();

        Ok(())
    }
}

pub(crate) struct RawDecoder(RawPtr, DecodeMessage);

impl Decoder for RawDecoder {
    type Item = RawPtr;
    type Error = Status;

    fn decode(&mut self, src: &mut DecodeBuf<'_>) -> Result<Option<Self::Item>, Self::Error> {
        if !src.has_remaining() {
            return Ok(None);
        }

        let ba = src.copy_to_bytes(src.remaining());

        let message = self.1(self.0.0, ba.as_ptr(), ba.len());

        Ok(Some(RawPtr(message)))
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn c_byte_array_create(
    data: *mut c_void,
    ptr: *const u8,
    len: usize,
    free: FreeCByteArray,
) -> *mut CByteArray {
    println!("c_byte_array_create");

    Box::into_raw(Box::new(CByteArray {
        data: RawPtr(data),
        ptr,
        len,
        free,
    }))
}

#[unsafe(no_mangle)]
pub extern "C" fn c_byte_array_free(ptr: *mut CByteArray) {
    if ptr.is_null() {
        return;
    }
    unsafe {
        drop(Box::from_raw(ptr));
    }
}

impl Drop for CByteArray {
    fn drop(&mut self) {
        (self.free)(self.data.0)
    }
}
