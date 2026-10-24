plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bilgilimedia.mobile.media.preview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bilgilimedia.mobile.media.preview"
        minSdk = 26
        targetSdk = 36
        versionCode = 22
        versionName = "1.4.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-messaging")
}
