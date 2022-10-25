package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCMutableCallOptions
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

interface IOSKMStub<S : KMStub<S>> {

    val channel: KMChannel
    val callOptions: GRPCCallOptions

    fun build(channel: KMChannel, callOptions: GRPCCallOptions): S

    fun withMetadata(metadata: KMMetadata): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
        mutableOptions.setInitialMetadata(metadata.metadataMap.toMap())

        return build(channel, mutableOptions)
    }

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions

        val millis = (duration * unit.toMilliFactor).toDouble()
        val seconds = millis / 1000.0

        mutableOptions.setTimeout(seconds)

        return build(channel, mutableOptions)
    }
}