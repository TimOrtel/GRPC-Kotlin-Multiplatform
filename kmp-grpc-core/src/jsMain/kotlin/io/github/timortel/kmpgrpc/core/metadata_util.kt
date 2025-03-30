package io.github.timortel.kmpgrpc.core

val KMMetadata.jsMetadata: dynamic
    get() = {
        val json = js("{}")
        metadataMap.forEach { (key, value) ->
            json[key] = value
        }

        val jsoner = js("JSON")
        jsoner.stringify(json).toString()
    }