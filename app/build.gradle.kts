plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val hasReleaseSigning = !System.getenv("KEYSTORE_PASSWORD").isNullOrBlank() &&
    !System.getenv("KEY_PASSWORD").isNullOrBlank() &&
    !System.getenv("KEY_ALIAS").isNullOrBlank()

android {
    namespace = "br.com.bonecoliveia"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.bonecoliveia"
        minSdk = 23
        targetSdk = 35
        versionCode = 8
        versionName = "0.8.0"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file("boneco-release.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation("com.github.jwdeveloper.TikTok-Live-Java:Client:1.13.0-Release")
}
