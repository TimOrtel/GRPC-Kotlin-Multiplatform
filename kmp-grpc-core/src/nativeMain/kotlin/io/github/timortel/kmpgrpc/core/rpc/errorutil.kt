package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.Code
import io.github.timortel.kmpgrpc.core.Status
import platform.Foundation.NSError

val NSError.asGrpcStatus: Status
    get() =  Status(Code.getCodeForValue(code.toInt()), description.orEmpty())
