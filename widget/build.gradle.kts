plugins {
    id("com.android.library")
    id("maven-publish")
    kotlin("android")
    kotlin("kapt")
}

repositories {
    jcenter()
    google()
    mavenCentral()
    maven(url = uri("https://jitpack.io"))
}

publishing {
    publications {
        create("maven_public",MavenPublication::class) {
            groupId = "com.github.sieunju"
            artifactId = "widget"
            version = Apps.versionName
        }
    }
}

android {
    compileSdk = Apps.compileSdkVersion

    // ktlint
    lint {
        abortOnError = false
    }

    defaultConfig {
        minSdk = Apps.minSdkVersion
        targetSdk = Apps.targetSdkVersion
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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