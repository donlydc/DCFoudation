plugins {
    `kotlin-dsl`
}

group = "com.dc.android.foundation.buildlogic"

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
    implementation(libs.androidx.room.gradle.plugin)
    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        register("dcAndroidApplication") {
            id = "dc.android.application"
            implementationClass = "DcAndroidApplicationConventionPlugin"
        }
        register("dcAndroidLibrary") {
            id = "dc.android.library"
            implementationClass = "DcAndroidLibraryConventionPlugin"
        }
        register("dcKotlinJvm") {
            id = "dc.kotlin.jvm"
            implementationClass = "DcKotlinJvmConventionPlugin"
        }
        register("dcCompose") {
            id = "dc.compose"
            implementationClass = "DcComposeConventionPlugin"
        }
        register("dcHilt") {
            id = "dc.hilt"
            implementationClass = "DcHiltConventionPlugin"
        }
        register("dcRoom") {
            id = "dc.room"
            implementationClass = "DcRoomConventionPlugin"
        }
        register("dcTesting") {
            id = "dc.testing"
            implementationClass = "DcTestingConventionPlugin"
        }
        register("dcFeatureGenerator") {
            id = "dc.feature.generator"
            implementationClass = "DcFeatureGeneratorPlugin"
        }
    }
}
