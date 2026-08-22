import org.gradle.api.Plugin
import org.gradle.api.Project

class DcTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        addLibrary("testImplementation", "junit")
        addLibrary("testImplementation", "kotlinx-coroutines-test")
        addLibrary("testImplementation", "turbine")

        fun configureAndroidTests() {
            addLibrary("androidTestImplementation", "androidx-junit")
            addLibrary("androidTestImplementation", "androidx-espresso-core")
        }

        pluginManager.withPlugin("com.android.application") {
            configureAndroidTests()
        }
        pluginManager.withPlugin("com.android.library") {
            configureAndroidTests()
        }
        pluginManager.withPlugin("dc.compose") {
            addPlatform("androidTestImplementation", "androidx-compose-bom")
            addLibrary("androidTestImplementation", "androidx-compose-ui-test-junit4")
            addLibrary("debugImplementation", "androidx-compose-ui-test-manifest")
        }
    }
}
