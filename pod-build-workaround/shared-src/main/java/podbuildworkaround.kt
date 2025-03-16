import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildTask
import org.jetbrains.kotlin.konan.target.Family
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

// Workaround for https://youtrack.jetbrains.com/issue/KT-76035/Allow-extra-command-line-arguments-in-PodBuildTask
fun Project.replacePodBuildWithCustomPodBuildTask() {
    tasks.withType(PodBuildTask::class.java)
        .filter { it.name.contains("ProtoRPC") || it.name.contains("podBuildProtobuf") }
        .forEach { oldTask: PodBuildTask ->
            val newTaskName = oldTask.name + "-patched"
            val newTask = tasks.register(newTaskName, CustomPodBuildTask::class.java) { newTask ->
                group = oldTask.group
                description = oldTask.description
                newTask.buildSettingsFile.set(oldTask.buildSettingsFile)

                val data = extractPodBuildTaskData(oldTask)
                newTask.pod.set(data.pod.get())
                newTask.family.set(data.family.get())
                newTask.podsXcodeProjDir.set(data.podsXcodeProjDir.get())
                newTask.appleTarget.set(extractAppleTarget(data.appleTarget))
                oldTask.dependsOn.forEach {
                    it as TaskProvider<*>
                    if (it.get() != newTask) {
                        newTask.dependsOn(it.get())
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
