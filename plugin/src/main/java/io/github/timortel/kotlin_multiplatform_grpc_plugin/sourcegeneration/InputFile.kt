package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import java.io.InputStream

interface InputFile {

    val name: String
    val nameWithoutExtension: String

    val isDirectory: Boolean
    val isProtoFile: Boolean

    val files: List<InputFile>

    val path: String

    fun inputStream(): InputStream
}
