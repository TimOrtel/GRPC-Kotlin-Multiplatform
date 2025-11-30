package io.github.timortel.kotlin_multiplatform_grpc_plugin

fun createSingleFileProtoFolder(fileHeader: String, content: String): FakeInputDirectory {
    return FakeInputDirectory("dir", listOf(createProtoFile(fileHeader, content)))
}

fun createProtoFile(fileHeader: String, content: String, name: String = "testFile"): FakeInputFile {
    val protoContent = """
        $fileHeader
        
        $content
    """.trimIndent()

    return FakeInputFile(name, protoContent)
}

fun createSingleFileProtoFolderFromResource(classLoader: ClassLoader, fileName: String): FakeInputDirectory {
    val content = classLoader.getResourceAsStream(fileName)!!.reader().readText()
    return FakeInputDirectory("dir", listOf(FakeInputFile(fileName, content)))
}
