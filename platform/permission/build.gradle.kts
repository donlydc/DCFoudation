plugins {
    id("dc.android.library")
    id("dc.compose")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.platform.permission"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
