plugins {
    id("dc.android.library")
    id("dc.compose")
}

android {
    namespace = "com.dc.android.foundation.platform.permission"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
