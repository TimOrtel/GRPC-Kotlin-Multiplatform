package io.github.timortel.kmpgrpc.plugin.sourcegeneration

import java.io.File
import java.io.InputStream

class SystemInputFile(private val file: File) : InputFile {

    override val isDirectory: Boolean
        get() = file.isDirectory
    override val isProtoFile: Boolean
        get() = file.isFile && file.extension == "proto"
    override val files: List<InputFile>
        get() = file.listFiles()?.map { SystemInputFile(it) }.orEmpty()

    override val name: String
        get() = file.name

    override val nameWithoutExtension: String
        get() = file.nameWithoutExtension

    override val path: String
        get() = file.path

    override fun inputStream(): InputStream = file.inputStream()
}
