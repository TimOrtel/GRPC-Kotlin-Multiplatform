package io.github.timortel.kotlin_multiplatform_grpc_plugin.generation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import org.slf4j.LoggerFactory

abstract class BaseGenerationTest {

    protected val logger = LoggerFactory.getLogger("DeprecatedOptionGenerationTest")

    protected val targetMapAll = mapOf(
        SourceTarget.Common to true,
        SourceTarget.Js to true,
        SourceTarget.Jvm to true,
        SourceTarget.Native to true
    )
}
