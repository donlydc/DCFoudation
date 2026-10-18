plugins {
    id("dc.android.library")
    id("dc.testing")
}

android {
    namespace = "com.dc.android.foundation.testing"
}

dependencies {
    // These are API dependencies because the reusable fakes/rules expose their types.
    api(project(":runtime:concurrency"))
    api(project(":data:preferences"))
    api(project(":observability"))
    api(project(":runtime:scheduling"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
}
