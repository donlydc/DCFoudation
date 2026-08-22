import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DcRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        addLibrary("implementation", "androidx-room-runtime")
        addLibrary("implementation", "androidx-room-ktx")
        addLibrary("ksp", "androidx-room-compiler")
    }
}
