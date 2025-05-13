use crate::rawpointer::RawPtr;
use bytes::{Buf, BufMut};
use std::os::raw::c_void;
use std::ptr::null_mut;
use std::slice::from_raw_parts;
use log::trace;
use tonic::codec::{DecodeBuf, EncodeBuf};
use tonic::{
    Status,
    codec::{Codec, Decoder, Encoder},
};

pub type FreeCByteArray = extern "C" fn(*mut c_void);

pub struct CByteArray {
    data: RawPtr,
    ptr: *const u8,
    len: usize,
    free: FreeCByteArray,
}

pub type EncodeMessage = extern "C" fn(*mut c_void) -> *mut CByteArray;
pub type DecodeMessage = extern "C" fn(*mut c_void, ptr: *const u8, len: usize) -> *mut c_void;

#[derive(Clone)]
pub(crate) struct RawBytesCodec {
    pub(crate) encode: EncodeMessage,
    pub(crate) decode: DecodeMessage,
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
        }
    }

    fn decoder(&mut self) -> Self::Decoder {
        RawDecoder(self.user_data, self.decode)
    }
}

pub(crate) struct RawEncoder {
    pub(crate) encode_message: EncodeMessage,
}

impl Encoder for RawEncoder {
    type Item = RawPtr;
    type Error = Status;

    fn encode(&mut self, item: Self::Item, dst: &mut EncodeBuf<'_>) -> Result<(), Self::Error> {
        trace!("encode()");
        let ba = unsafe { Box::from_raw((self.encode_message)(item.0)) };

        if ba.len == 0 {
            trace!("encode() - empty array");
            return Ok(());
        }

        unsafe {
            dst.put_slice(from_raw_parts(ba.ptr, ba.len));
        }
        

        trace!("encode() - done");

        Ok(())
    }
}

pub(crate) struct RawDecoder(RawPtr, DecodeMessage);

impl Decoder for RawDecoder {
    type Item = RawPtr;
    type Error = Status;

    fn decode(&mut self, src: &mut DecodeBuf<'_>) -> Result<Option<Self::Item>, Self::Error> {
        trace!("decode()");
        if !src.has_remaining() {
            trace!("decode() - empty message received");
            return Ok(Some(RawPtr(self.1(self.0.0, null_mut(), 0))));
        }
        
        let ba = src.copy_to_bytes(src.remaining());

        trace!("decode() - copied bytes");

        let message = self.1(self.0.0, ba.as_ptr(), ba.len());

        trace!("decode() - returning with message");

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
    trace!("c_byte_array_create()");

    Box::into_raw(Box::new(CByteArray {
        data: RawPtr(data),
        ptr,
        len,
        free,
    }))
}

#[unsafe(no_mangle)]
pub extern "C" fn c_byte_array_free(ptr: *mut CByteArray) {
    trace!("c_byte_array_free() - ptr: {:p}", ptr);

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
