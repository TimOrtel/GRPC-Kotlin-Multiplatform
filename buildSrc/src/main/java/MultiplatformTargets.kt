import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

enum class TargetGroup {
    ALL,
    APPLE_TEST,
    OTHERS_TEST
}

fun Project.getTargetGroup(): TargetGroup {
    val targetsTargetProperty = "io.github.timortel.kmp-grpc.internal.native.targets"
    return if (project.hasProperty(targetsTargetProperty)) {
        when (project.property(targetsTargetProperty).toString()) {
            "appleTest" -> TargetGroup.APPLE_TEST
            "othersTest" -> TargetGroup.OTHERS_TEST
            else -> TargetGroup.ALL
        }
    } else TargetGroup.ALL
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.setupTargets(project: Project) {
    val targetsTarget = project.getTargetGroup()

    val includeAllTargets = targetsTarget == TargetGroup.ALL

    val includeAppleTest = targetsTarget == TargetGroup.APPLE_TEST
    val includeOthersTest = targetsTarget == TargetGroup.OTHERS_TEST

    if (includeAllTargets || includeOthersTest) {
        androidTarget("android") {
            publishLibraryVariants("release", "debug")
        }

        js(IR) {
            browser()
            nodejs()
        }

        wasmJs {
            browser()
            nodejs()
        }

        jvm("jvm")

        linuxX64()
        linuxArm64()
    }

    if (includeAllTargets || includeAppleTest) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()

        macosArm64()
        macosX64()
    }

    if (includeAllTargets) {
        mingwX64()
    }
}
