package io.github.timortel.kmpgrpc.core

import kotlin.js.json

val KMMetadata.jsMetadata: Metadata
    get() = json(
        *metadataMap.entries.map { (key, value) -> key to value }.toTypedArray()
    ).unsafeCast<Metadata>()
