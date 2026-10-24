plugins {
    id("com.android.application")
}

android {
    namespace = "com.bilgilimedia.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bilgilimedia.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 23
        versionName = "1.4.9"

        buildConfigField("String", "FIREBASE_APP_ID", "\"1:190776938532:android:714bfdfa9b2ab78c4788b1\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"AIzaSyD22yhT86xghiTMyUPQMUuK_wxQwR_6Ywg\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"bilgilimedia-60d6f\"")
        buildConfigField("String", "FIREBASE_SENDER_ID", "\"190776938532\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"bilgilimedia-60d6f.firebasestorage.app\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-messaging")
}
