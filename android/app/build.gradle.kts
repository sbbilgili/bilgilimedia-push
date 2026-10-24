plugins {
    id("com.android.application")
}

fun env(name: String): String = System.getenv(name) ?: ""

android {
    namespace = "com.bilgilimedia.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bilgilimedia.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 23
        versionName = "1.4.9"

        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"bilgilimedia-60d6f\"")
        buildConfigField("String", "FIREBASE_SENDER_ID", "\"190776938532\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"" + env("BILGILI_FIREBASE_API_KEY") + "\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"" + env("BILGILI_FIREBASE_APP_ID") + "\"")
    }

    flavorDimensions += "env"
    productFlavors {
        create("qa") {
            dimension = "env"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            buildConfigField("boolean", "FIREBASE_ENABLED", "false")
        }
        create("production") {
            dimension = "env"
            buildConfigField("boolean", "FIREBASE_ENABLED", "true")
            buildConfigField("String", "FIREBASE_APP_ID", "\"1:190776938532:android:714bfdfa9b2ab78c4788b1\"")
        }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-messaging")
}
