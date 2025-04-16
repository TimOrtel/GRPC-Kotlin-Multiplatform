package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.KMCode
import io.github.timortel.kmpgrpc.core.KMStatus
import platform.Foundation.NSError

val NSError.asGrpcStatus: KMStatus
    get() =  KMStatus(KMCode.getCodeForValue(code.toInt()), description.orEmpty())
