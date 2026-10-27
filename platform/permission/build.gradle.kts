plugins {
    id("dc.android.library")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.platform.permission"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
