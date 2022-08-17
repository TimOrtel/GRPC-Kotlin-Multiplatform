package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.getJVMClassName

data class ProtoMessage(
    val pkg: String,
    val name: String,
    val attributes: List<ProtoMessageAttribute>,
    val oneOfs: List<ProtoOneOf>,
    val enums: List<ProtoEnum>,
    val parent: ProtoMessage?,
    val children: List<ProtoMessage>,
    val protoFileName: String,
    val javaUseMultipleFiles: Boolean,
) {
    val commonName = "KM${name.capitalize()}"

    val commonType: ClassName = parent?.commonType?.nestedClass(commonName) ?: ClassName(pkg, commonName)
    val jsType: ClassName =
        parent?.jsType?.nestedClass("JS_${name.capitalize()}") ?: ClassName(pkg, "JS_${name.capitalize()}")
    val jvmType: ClassName = parent?.jvmType?.nestedClass(name) ?: getJVMClassName(pkg, protoFileName, javaUseMultipleFiles, listOf(name))
    val iosType = commonType

    override fun toString(): String = name
}
