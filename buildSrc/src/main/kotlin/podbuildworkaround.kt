import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildTask
import org.jetbrains.kotlin.konan.target.Family
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

fun Project.replacePodBuildWithCustomPodBuild() {
    tasks.withType<PodBuildTask>().forEach { oldTask: PodBuildTask ->
        val newTaskName = oldTask.name + "-patched"
        val newTask = tasks.register(newTaskName, CustomPodBuildTask::class) {
            group = oldTask.group
            description = oldTask.description
            buildSettingsFile.set(oldTask.buildSettingsFile)

            val data = extractPodBuildTaskData(oldTask)
            pod.set(data.pod.get())
            family.set(data.family.get())
            podsXcodeProjDir.set(data.podsXcodeProjDir.get())
            appleTarget.set(extractAppleTarget(data.appleTarget))
            oldTask.dependsOn.forEach {
                it as TaskProvider<*>
                if (it.get() != this) {
                    dependsOn(it.get())
                }
            }
        }

        oldTask.isEnabled = false
        oldTask.dependsOn(newTask)
    }
}

data class PodBuildTaskData(
    val pod: Property<CocoapodsDependency>,
    val appleTarget: Property<Any>,
    val family: Property<Family>,
    val podsXcodeProjDir: DirectoryProperty
)

@Suppress("UNCHECKED_CAST")
fun extractPodBuildTaskData(task: PodBuildTask): PodBuildTaskData {
    val properties = PodBuildTask::class.declaredMemberProperties.associate {
        it.isAccessible = true
        it.name to it.get(task)
    }

    return PodBuildTaskData(
        pod = properties["pod"] as Property<CocoapodsDependency>,
        appleTarget = properties["appleTarget"] as Property<Any>,
        family = properties["family"] as Property<Family>,
        podsXcodeProjDir = properties["podsXcodeProjDir"] as DirectoryProperty
    )
}

fun extractAppleTarget(prop: Property<Any>): String {
    val instance = prop.get()
    val nameMember = instance::class.members.first { it.name == "name" }
    val name = nameMember.call(instance)
    return when (name) {
        "MACOS_DEVICE" -> "macOS"
        "IPHONE_DEVICE" -> "iOS"
        "IPHONE_SIMULATOR" -> "iOS Simulator"
        "WATCHOS_DEVICE" -> "watchOS"
        "WATCHOS_SIMULATOR" -> "watchOS Simulator"
        "TVOS_DEVICE" -> "tvOS"
        "TVOS_SIMULATOR" -> "tvOS Simulator"
        else -> throw IllegalStateException()
    }
}