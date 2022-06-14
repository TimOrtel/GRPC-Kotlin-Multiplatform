package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types

class ProtoRpc(
    val rpcName: String,
    val request: Types,
    val response: Types,
    val isResponseStream: Boolean,
)