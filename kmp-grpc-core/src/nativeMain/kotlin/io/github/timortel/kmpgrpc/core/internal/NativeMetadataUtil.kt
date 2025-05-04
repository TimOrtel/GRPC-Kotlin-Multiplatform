package io.github.timortel.kmpgrpc.core.internal

import cnames.structs.metadata
import io.github.timortel.kmpgrpc.native.*
import kotlinx.cinterop.*
import kotlin.reflect.KFunction3

/**
 * Extracts metadata from a native metadata object and converts it into a Kotlin Map.
 *
 * @param nativeMetadata The native metadata object of type `CValue<metadata>` to extract data from.
 * @return A map containing metadata key-value pairs as strings, converted from the native metadata.
 */
internal fun getMetadataFromNativeMetadata(nativeMetadata: CPointer<metadata>?): Map<String, String> {
    return getMetadataFromNativeMetadata(nativeMetadata, ::metadata_iterate)
}

/**
 * Extracts metadata from a native metadata object and converts it into a Kotlin Map.
 *
 * @param nativeMetadata The native metadata object of type `CValue<metadata_const>` to extract data from.
 * @return A map containing metadata key-value pairs as strings, converted from the native metadata.
 */
internal fun getMetadataFromNativeMetadata(nativeMetadata: CPointer<cnames.structs.metadata_const>?): Map<String, String> {
    return getMetadataFromNativeMetadata(nativeMetadata, ::metadata_const_iterate)
}

private fun <T : CPointed> getMetadataFromNativeMetadata(
    nativeMetadata: CPointer<T>?,
    iterate: KFunction3<CValuesRef<T>?, CValuesRef<*>?, CPointer<CFunction<(COpaquePointer?, CPointer<ByteVar>?, CPointer<ByteVar>?) -> Unit>>?, Unit>
): Map<String, String> {
    return buildMap {
        iterate(
            nativeMetadata,
            StableRef.create(this).asCPointer(),
            staticCFunction { data, key, value ->
                val mutableMap = data!!.asStableRef<MutableMap<String, String>>().get()

                mutableMap.put(key!!.toKString(), value!!.toKString())
            }
        )
    }
}
