import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

enum class TargetGroup {
    ALL,
    NATIVE_APPLE,
    NATIVE_OTHERS_TESTABLE,
    JS,
    JVM,
    NON_APPLE_TESTABLE
}

private enum class Targets {
    NATIVE_APPLE,
    NATIVE_OTHERS_TESTABLE,
    NATIVE_OTHERS_NON_TESTABLE,
    JS,
    JVM
}

fun Project.getTargetGroup(): TargetGroup {
    val targetsTargetProperty = "io.github.timortel.kmp-grpc.internal.targets"
    return if (project.hasProperty(targetsTargetProperty)) {
        TargetGroup.values().firstOrNull { it.name == project.property(targetsTargetProperty).toString() }
            ?: TargetGroup.ALL
    } else TargetGroup.ALL
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.setupTargets(project: Project) {
    val targetsTarget = project.getTargetGroup()

    val enabledTargets = when(targetsTarget) {
        TargetGroup.ALL -> Targets.values().toList()
        TargetGroup.NATIVE_APPLE -> listOf(Targets.NATIVE_APPLE)
        TargetGroup.NATIVE_OTHERS_TESTABLE -> listOf(Targets.NATIVE_OTHERS_TESTABLE)
        TargetGroup.JS -> listOf(Targets.JS)
        TargetGroup.JVM -> listOf(Targets.JVM)
        TargetGroup.NON_APPLE_TESTABLE -> listOf(Targets.JVM, Targets.JS, Targets.NATIVE_OTHERS_TESTABLE)
    }

    // Android always has to be included.
    androidTarget("android") {
        publishLibraryVariants("release", "debug")
    }

    if (Targets.JS in enabledTargets) {
        js(IR) {
            browser()
            nodejs()
        }

        wasmJs {
            browser()
            nodejs()
        }
    }

    if (Targets.JVM in enabledTargets) {
        jvm("jvm")
    }

    if (Targets.NATIVE_OTHERS_TESTABLE in enabledTargets) {
        linuxX64()
        linuxArm64()
    }

    if (Targets.NATIVE_APPLE in enabledTargets) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()

        macosArm64()
        macosX64()
    }

    if (Targets.NATIVE_OTHERS_NON_TESTABLE in enabledTargets) {
        mingwX64()
    }
}

fun Project.setupTestsTask() {
    val targetsTarget = project.getTargetGroup()

    if (targetsTarget == TargetGroup.NATIVE_APPLE || targetsTarget == TargetGroup.ALL) {
        project.tasks.register("appleTest") {
            dependsOn("iosX64Test")
            dependsOn("iosSimulatorArm64Test")
            dependsOn("macosArm64Test")
            dependsOn("macosX64Test")
        }
    }

    if (targetsTarget == TargetGroup.NON_APPLE_TESTABLE || targetsTarget == TargetGroup.ALL) {
        project.tasks.register("othersTest") {
            dependsOn("jsTest")
            dependsOn("wasmJsTest")
            dependsOn("jvmTest")
            dependsOn("linuxX64Test")
        }
    }

    if (targetsTarget == TargetGroup.NATIVE_OTHERS_TESTABLE) {
        project.tasks.register("othersTest") {
            dependsOn("linuxX64Test")
        }
    }
}
