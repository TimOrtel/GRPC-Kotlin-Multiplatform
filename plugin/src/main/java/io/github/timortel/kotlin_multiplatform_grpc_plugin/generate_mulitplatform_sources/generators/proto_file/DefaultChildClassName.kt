package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.ClassName

interface DefaultChildClassName {

    fun getChildClassName(parentClass: ClassName?, childName: String, pkg: String): ClassName {
        return parentClass?.nestedClass(childName) ?: ClassName(pkg, childName)
    }
}