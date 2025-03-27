package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.InputFile
import java.io.InputStream

class FakeInputDirectory(override val name: String, override val files: List<InputFile>, override val path: String = "") : InputFile {

    override val nameWithoutExtension: String = ""

    override val isDirectory: Boolean = true

    override val isProtoFile: Boolean = false

    override fun inputStream(): InputStream {
        throw UnsupportedOperationException()
    }
}
