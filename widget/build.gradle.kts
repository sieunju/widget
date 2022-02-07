plugins {
    id("com.android.library")
    id("com.github.dcendents.android-maven")
    id("maven-publish")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(Apps.compileSdkVersion)

    // ktlint
    lintOptions {
        isAbortOnError = false
    }

    defaultConfig {
        minSdkVersion(Apps.minSdkVersion)
        targetSdkVersion(Apps.targetSdkVersion)
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(AndroidX.ktx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.material)
    implementation(AndroidX.palette)
    implementation(Coroutine.core)
    implementation(Coroutine.android)
    implementation(Rx.base)
    implementation(Rx.android)
    implementation(Rx.kotlin)

    implementation(AndroidX.lifecycle)
    implementation(AndroidX.liveData)
}
