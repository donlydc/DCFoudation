import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DcComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            configureCompose()
        }
        pluginManager.withPlugin("com.android.library") {
            configureCompose()
        }
    }
}

private fun Project.configureCompose() {
    extensions.configure<CommonExtension> {
        buildFeatures.compose = true
    }

    addPlatform("implementation", "androidx-compose-bom")
    addLibrary("implementation", "androidx-compose-ui")
    addLibrary("implementation", "androidx-compose-ui-graphics")
    addLibrary("implementation", "androidx-compose-ui-tooling-preview")
    addLibrary("implementation", "androidx-compose-material3")
    addLibrary("debugImplementation", "androidx-compose-ui-tooling")
}
