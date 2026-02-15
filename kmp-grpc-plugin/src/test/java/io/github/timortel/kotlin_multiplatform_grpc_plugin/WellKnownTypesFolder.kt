package io.github.timortel.kotlin_multiplatform_grpc_plugin

val wellKnownTypesFolder = FakeInputDirectory(
    name = "google",
    path = "google",
    files = listOf(
        FakeInputDirectory(
            name = "protobuf",
            path = "protobuf",
            files = listOf(
                FakeInputFile(
                    name = "descriptor.proto",
                    content = Thread.currentThread().contextClassLoader.getResourceAsStream("google/protobuf/descriptor.proto")
                        .use { inputStream ->
                            inputStream!!.bufferedReader().use { bufferedReader ->
                                 bufferedReader.readText()
                            }
                        }
                )
            )
        )
    )
)
