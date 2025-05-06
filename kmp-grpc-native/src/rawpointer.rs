use std::os::raw::c_void;

#[derive(Clone, Copy)]
pub struct RawPtr(pub(crate) *mut c_void);

unsafe impl Send for RawPtr {}
unsafe impl Sync for RawPtr {}
