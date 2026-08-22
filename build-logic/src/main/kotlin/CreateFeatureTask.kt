import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CreateFeatureTask : DefaultTask() {
    @get:Input
    abstract val featureName: Property<String>

    @get:Input
    abstract val basePackage: Property<String>

    @get:Input
    abstract val navigation: Property<Boolean>

    @get:Input
    abstract val network: Property<Boolean>

    @get:Input
    abstract val room: Property<Boolean>

    @get:Input
    abstract val permission: Property<Boolean>

    @get:Input
    abstract val backgroundWork: Property<Boolean>

    @get:Internal
    abstract val mainSourceRoot: DirectoryProperty

    @get:Internal
    abstract val testSourceRoot: DirectoryProperty

    init {
        group = "dc scaffolding"
        description = "Creates an explicit UDF feature scaffold without overwriting existing files."
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun createFeature() {
        val generated = try {
            FeatureScaffoldGenerator.generate(
                mainSourceRoot = mainSourceRoot.get().asFile.toPath(),
                testSourceRoot = testSourceRoot.get().asFile.toPath(),
                options = FeatureGeneratorOptions(
                    featureName = featureName.get(),
                    basePackage = basePackage.get(),
                    navigation = navigation.get(),
                    network = network.get(),
                    room = room.get(),
                    permission = permission.get(),
                    backgroundWork = backgroundWork.get()
                )
            )
        } catch (error: IllegalArgumentException) {
            throw GradleException(error.message ?: "Invalid feature generator input.", error)
        } catch (error: IllegalStateException) {
            throw GradleException(error.message ?: "Feature generator output conflict.", error)
        }

        logger.lifecycle(
            "Created feature {} ({}) with {} files:\n{}",
            generated.classPrefix,
            generated.packageName,
            generated.files.size,
            generated.files.joinToString("\n") { "- ${project.relativePath(it.toFile())}" }
        )
    }
}
