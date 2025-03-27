package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.InputFile
import java.io.InputStream

class FakeInputFile(override val name: String, private val content: String) : InputFile {
    override val nameWithoutExtension: String = name

    override val isDirectory: Boolean = false
    override val isProtoFile: Boolean = true
    override val files: List<InputFile> = emptyList()
    override val path: String = ""

    override fun inputStream(): InputStream = content.byteInputStream()
}
