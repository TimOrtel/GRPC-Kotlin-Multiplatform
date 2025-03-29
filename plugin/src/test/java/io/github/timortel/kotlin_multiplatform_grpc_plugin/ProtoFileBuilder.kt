package io.github.timortel.kotlin_multiplatform_grpc_plugin

fun createSingleFileProtoFolder(content: String): FakeInputDirectory {
    return FakeInputDirectory("dir", listOf(createProto3File(content)))
}

fun createProto3File(content: String, name: String = "testFile"): FakeInputFile {
    val protoContent =  """
        syntax = "proto3";
        
        $content
    """.trimIndent()

    return FakeInputFile(name, protoContent)
}
