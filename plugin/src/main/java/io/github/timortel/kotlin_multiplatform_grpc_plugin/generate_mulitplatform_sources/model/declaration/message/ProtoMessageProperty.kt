package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message

interface ProtoMessageProperty {
    /**
     * The name of the property as defined in the proto source code
     */
    val name: String

    /**
     * The name of the field in the generated message class
     */
    val fieldName: String
}
