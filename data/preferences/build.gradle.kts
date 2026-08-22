plugins {
    id("dc.android.library")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.data.preferences"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.javax.inject)
}
