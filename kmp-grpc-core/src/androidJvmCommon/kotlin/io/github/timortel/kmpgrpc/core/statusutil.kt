package io.github.timortel.kmpgrpc.core

import io.grpc.Status

val KMStatus.jvmStatus: Status
    get() = Status.fromCodeValue(code.value).withDescription(statusMessage)
