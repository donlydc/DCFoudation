import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

class DcFeatureGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val inferredPackage = providers.provider {
                providers.gradleProperty("featurePackage").orNull
                    ?: extensions.findByType(ApplicationExtension::class.java)?.namespace
                    ?: extensions.findByType(LibraryExtension::class.java)?.namespace
                    ?: throw GradleException(
                        "Could not infer the feature package. Apply dc.feature.generator to an Android " +
                            "module or pass -PfeaturePackage=com.example.app."
                    )
            }

            tasks.register("createFeature", CreateFeatureTask::class.java) {
                featureName.convention(providers.gradleProperty("featureName").orElse(""))
                basePackage.convention(inferredPackage)
                navigation.convention(booleanProperty("navigation"))
                network.convention(booleanProperty("network"))
                room.convention(booleanProperty("room"))
                permission.convention(booleanProperty("permission"))
                backgroundWork.convention(booleanProperty("backgroundWork"))
                mainSourceRoot.convention(layout.projectDirectory.dir("src/main/java"))
                testSourceRoot.convention(layout.projectDirectory.dir("src/test/java"))
            }
        }
    }

    private fun Project.booleanProperty(name: String): Provider<Boolean> =
        providers.gradleProperty(name).map { raw ->
            when (raw.lowercase()) {
                "true" -> true
                "false" -> false
                else -> throw GradleException("-P$name must be true or false, but was '$raw'.")
            }
        }.orElse(false)
}
