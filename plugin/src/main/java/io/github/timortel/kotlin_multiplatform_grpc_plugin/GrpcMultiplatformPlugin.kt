package io.github.timortel.kotlin_multiplatform_grpc_plugin

import io.github.timortel.kotlin_multiplatform_grpc_plugin.configuration.GrpcProtobufConfiguration
import io.github.timortel.kotlin_multiplatform_grpc_plugin.configuration.ProjectSetupConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class GrpcMultiplatformPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Configure project
        ProjectSetupConfiguration.configure(project)

        // Configure generation of sources
        GrpcProtobufConfiguration.configure(project)
    }
}