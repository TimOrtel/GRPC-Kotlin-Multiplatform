package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName

fun getCommonClassName(pkg: String, baseFileNames: List<String>): ClassName =
    ClassName(pkg, baseFileNames.map { "KM$it" })

@Suppress("DefaultLocale")
fun getJVMClassName(
    pkg: String,
    protoFileName: String,
    useMultipleFiles: Boolean,
    baseFileNames: List<String>
): ClassName {
    return if (useMultipleFiles) ClassName(pkg, baseFileNames)
    else ClassName(pkg, listOf(protoFileName.capitalize()) + baseFileNames)
}

fun getJSClassName(pkg: String, baseFileNames: List<String>): ClassName = ClassName(pkg, baseFileNames.map { "JS_$it" })