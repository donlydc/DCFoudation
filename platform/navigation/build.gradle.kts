plugins {
    id("dc.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dc.android.foundation.platform.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
