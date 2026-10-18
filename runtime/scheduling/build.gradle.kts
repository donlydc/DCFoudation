plugins {
    id("dc.android.library")
    id("dc.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dc.android.foundation.runtime.scheduling"
}

dependencies {
    implementation(project(":foundation"))
    implementation(project(":runtime:concurrency"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
