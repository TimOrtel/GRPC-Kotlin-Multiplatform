package io.github.timortel.kmpgrpc.plugin

import io.github.timortel.kmpgrpc.plugin.configuration.GrpcProtobufConfiguration
import io.github.timortel.kmpgrpc.plugin.configuration.ProjectSetupConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpGrpcPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Configure project
        ProjectSetupConfiguration.configure(project)

        // Configure generation of sources
        GrpcProtobufConfiguration.configure(project)
    }
}