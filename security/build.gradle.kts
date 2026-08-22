plugins {
    id("dc.android.library")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.security"
}

dependencies {
    implementation(project(":runtime:concurrency"))
    implementation(libs.androidx.security.crypto)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
