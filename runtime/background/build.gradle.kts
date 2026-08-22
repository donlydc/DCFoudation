plugins {
    id("dc.android.library")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.runtime.background"
}

dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
