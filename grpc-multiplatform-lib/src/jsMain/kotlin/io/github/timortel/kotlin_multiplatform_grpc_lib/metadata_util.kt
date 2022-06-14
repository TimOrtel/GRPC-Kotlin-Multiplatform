package io.github.timortel.kotlin_multiplatform_grpc_lib

val KMMetadata.jsMetadata: dynamic
    get() = {
        val json = js("{}")
        metadataMap.forEach { (key, value) ->
            json[key] = value
        }

        val jsoner = js("JSON")
        jsoner.stringify(json).toString()
    }