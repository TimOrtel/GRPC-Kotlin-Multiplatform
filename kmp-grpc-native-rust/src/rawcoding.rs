use crate::rawpointer::RawPtr;
use bytes::{Buf, BufMut};
use std::os::raw::c_void;
use std::slice::from_raw_parts;
use tonic::codec::{DecodeBuf, EncodeBuf};
use tonic::{
    codec::{Codec, Decoder, Encoder},
    Status,
};

pub struct CByteArray {
    ptr: *const u8,
    len: usize,
}

pub type EncodeMessage = extern "C" fn(*mut c_void) -> *mut CByteArray;
pub type DecodeMessage = extern "C" fn(ptr: *const u8, len: usize) -> *mut c_void;

#[derive(Clone)]
pub(crate) struct RawBytesCodec {
    pub(crate) encode: EncodeMessage,
    pub(crate) decode: DecodeMessage,
}

impl Codec for RawBytesCodec {
    type Encode = RawPtr;
    type Decode = RawPtr;

    type Encoder = RawEncoder;
    type Decoder = RawDecoder;

    fn encoder(&mut self) -> Self::Encoder {
        RawEncoder(self.encode)
    }

    fn decoder(&mut self) -> Self::Decoder {
        RawDecoder(self.decode)
    }
}

pub(crate) struct RawEncoder(EncodeMessage);

impl Encoder for RawEncoder {
    type Item = RawPtr;
    type Error = Status;

    fn encode(&mut self, item: Self::Item, dst: &mut EncodeBuf<'_>) -> Result<(), Self::Error> {
        let ba = unsafe { self.0(item.0).as_mut().unwrap() };

        unsafe {
            dst.put_slice(from_raw_parts(ba.ptr, ba.len));
        }

        c_byte_array_free(ba);
        
        Ok(())
    }
}

pub(crate) struct RawDecoder(DecodeMessage);

impl Decoder for RawDecoder {
    type Item = RawPtr;
    type Error = Status;

    fn decode(&mut self, src: &mut DecodeBuf<'_>) -> Result<Option<Self::Item>, Self::Error> {
        if !src.has_remaining() {
            return Ok(None);
        }

        let ba = src.copy_to_bytes(src.remaining());

        let message = self.0(ba.as_ptr(), ba.len());

        Ok(Some(RawPtr(message)))
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn c_byte_array_create(ptr: *const u8, len: usize) -> *mut CByteArray {
    Box::into_raw(Box::new(CByteArray { ptr, len }))
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
