plugins {
    id("dc.android.library")
    alias(libs.plugins.kotlin.serialization)
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.data.network"
}

dependencies {
    implementation(project(":foundation"))
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    testImplementation(libs.okhttp.mockwebserver)
}
