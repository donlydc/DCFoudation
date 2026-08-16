plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.dc.android.foundation.security"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
}
