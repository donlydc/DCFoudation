plugins {
    id("dc.android.library")
    id("dc.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dc.android.foundation.platform.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
