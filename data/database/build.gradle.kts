plugins {
    id("dc.android.library")
    id("dc.room")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.data.database"
}

dependencies {
    implementation(project(":runtime:concurrency"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
