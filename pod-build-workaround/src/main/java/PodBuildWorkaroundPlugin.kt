import org.gradle.api.Plugin
import org.gradle.api.Project

class PodBuildWorkaroundPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            project.replacePodBuildWithCustomPodBuildTask()
        }
    }
}
