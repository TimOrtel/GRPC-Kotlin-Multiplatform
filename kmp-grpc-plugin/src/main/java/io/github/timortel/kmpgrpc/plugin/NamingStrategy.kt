package io.github.timortel.kmpgrpc.plugin

enum class NamingStrategy {
    /**
     * Keeps names exactly as defined in the .proto file.
     * Example: message_name -> message_name, field_name -> field_name
     */
    PROTO_LITERAL,

    /**
     * Transforms names to follow Kotlin conventions.
     * Example: message_name -> MessageName, field_name -> fieldName
     */
    KOTLIN_IDIOMATIC
}
