import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal const val DC_COMPILE_SDK = 37
internal const val DC_MIN_SDK = 24
internal const val DC_TARGET_SDK = 37
internal const val DC_JAVA_VERSION = 11

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureAndroid(extension: CommonExtension) {
    extension.compileSdk = DC_COMPILE_SDK
    extension.defaultConfig.minSdk = DC_MIN_SDK
    extension.compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    extension.compileOptions.targetCompatibility = JavaVersion.VERSION_11
    extension.compileOptions.isCoreLibraryDesugaringEnabled = true
    extension.lint.abortOnError = true
    extension.lint.checkReleaseBuilds = true
}

internal fun Project.addLibrary(configuration: String, alias: String) {
    dependencies.add(configuration, libs.findLibrary(alias).get())
}

internal fun Project.addPlatform(configuration: String, alias: String) {
    dependencies.add(configuration, dependencies.platform(libs.findLibrary(alias).get()))
}
