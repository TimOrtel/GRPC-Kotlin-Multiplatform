import org.gradle.api.Project

private val buildAsReleaseProperty = "io.github.timortel.kmp-grpc.internal.native.release"
val Project.buildAsRelease get() = if (project.hasProperty(buildAsReleaseProperty)) {
    project.property(buildAsReleaseProperty).toString() == "true"
} else false

private val usePrebuiltNativeProperty = "io.github.timortel.kmp-grpc.internal.native.prebuilt"
val Project.usePrebuiltNative get() = if (project.hasProperty(usePrebuiltNativeProperty)) {
    project.property(usePrebuiltNativeProperty).toString() == "true"
} else false
