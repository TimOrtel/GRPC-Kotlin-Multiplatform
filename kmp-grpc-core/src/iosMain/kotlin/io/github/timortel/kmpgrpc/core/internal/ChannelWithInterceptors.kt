package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.native.*
import kotlinx.cinterop.*

/**
 * Creates a native gRPC channel with the ability to apply a given interceptor to manipulate the call behavior.
 * The interceptor allows processing of headers and metadata during the creation of the channel.
 *
 * @param host The host address to which the channel should connect.
 * @param interceptor An implementation of [CallInterceptor] used to handle interception of gRPC method calls and metadata.
 * @return A pointer to the native gRPC channel if created successfully, or `null` if creation fails.
 */
internal fun createNativeChannelWithInterceptors(
    host: String,
    interceptor: CallInterceptor
): CPointer<cnames.structs.grpc_channel>? {
    return create_insecure_channel_with_interceptors(
        host = host,
        callback_data = StableRef.create(interceptor).asCPointer(),
        on_receive_initial_metadata = staticCFunction { data, methodDescriptor, metadata ->
            println("on_receive_initial_metadata()")
            try {
                val interceptor = data!!.asStableRef<CallInterceptor>().get()

                val (methodName, type) = methodDescriptor.useContents {
                    method_name!!.toKString() to when (type) {
                        CLIENT_STREAMING -> MethodDescriptor.MethodType.CLIENT_STREAMING
                        SERVER_STREAMING -> MethodDescriptor.MethodType.SERVER_STREAMING
                        BIDI_STREAMING -> MethodDescriptor.MethodType.BIDI_STREAMING
                        else -> MethodDescriptor.MethodType.UNARY
                    }
                }

                val mD = MethodDescriptor(methodName, type)

                val originalMetadata = Metadata.of(getMetadataFromNativeMetadata(metadata))

                val newMetadata = interceptor.onReceiveHeaders(mD, originalMetadata)

                // Perform a really quick comparison that the metadata has not changed. Then we can skip this.
                if (originalMetadata !== newMetadata) {
                    updateNativeMetadata(metadata, originalMetadata, newMetadata)
                }

                println("on_receive_initial_metadata() - pre3")
            } finally {
                metadata_destroy(metadata)
            }
            println("on_receive_initial_metadata() - done")
        }
    )
}

/**
 * Updates the native metadata with changes between the provided original metadata and new metadata.
 * It ensures that the native metadata reflects additions, updates, and removals based on the differences
 * between the original and new metadata.
 *
 * @param nativeMetadata the reference to the native metadata structure to be updated. Can be null.
 * @param originalMetadata the existing metadata to be used as the base for comparison.
 * @param newMetadata the new metadata containing updated or additional entries.
 */
private fun updateNativeMetadata(nativeMetadata: CPointer<cnames.structs.metadata>?, originalMetadata: Metadata, newMetadata: Metadata) {
    // Check for new entries
    newMetadata.entries.forEach { entry ->
        if (entry !in originalMetadata.entries.entries) {
            if (entry.key in originalMetadata.entries) {
                // Need to remove the entry for a clean state first
                metadata_remove(nativeMetadata, entry.key)
            }

            metadata_insert(nativeMetadata, entry.key, entry.value)
        }
    }

    // Check for deleted entries
    originalMetadata.entries.forEach { entry ->
        if (entry !in newMetadata.entries.entries) {
            metadata_remove(nativeMetadata, entry.key)
        }
    }
}
