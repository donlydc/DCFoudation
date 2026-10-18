plugins {
    id("dc.android.application")
    id("dc.compose")
    alias(libs.plugins.kotlin.serialization)
    id("dc.hilt")
    id("dc.room")
    id("dc.testing")
    id("dc.feature.generator")
}

android {
    namespace = "com.dc.android.foundation"

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "com.dc.android.foundation"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":foundation"))
    implementation(project(":ui:designsystem"))
    implementation(project(":runtime:concurrency"))
    implementation(project(":data:preferences"))
    implementation(project(":data:network"))
    implementation(project(":data:database"))
    implementation(project(":observability"))
    implementation(project(":platform:navigation"))
    implementation(project(":platform:permission"))
    implementation(project(":runtime:scheduling"))
    implementation(project(":security"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(project(":testing"))
}
