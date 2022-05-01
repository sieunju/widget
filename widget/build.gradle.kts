plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    // id("maven-publish")
    `maven-publish`
}

group = "com.github.sieunju"
version = Apps.versionName

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

//afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release"){
                groupId = Apps.groupId
                artifactId = Apps.artifactId
                version = Apps.versionName
            }
//            // Creates a Maven publication called "release".
//            release(MavenPublication) {
//                // Applies the component for the release build variant.
//                from components.release
//
//                        // You can then customize attributes of the publication as shown below.
//                        groupId = 'com.example.MyLibrary'
//                artifactId = 'final'
//                version = '1.0'
//            }
//            // Creates a Maven publication called “debug”.
//            debug(MavenPublication) {
//                // Applies the component for the debug build variant.
//                from components . debug
//
//                        groupId = 'com.example.MyLibrary'
//                artifactId = 'final-debug'
//                version = '1.0'
//            }
        }
    }
//}

//publishing {
//    publications {
//        create("maven_public",MavenPublication::class) {
//            groupId = "com.github.sieunju"
//            artifactId = "widget"
//            version = Apps.versionName
//        }
//    }
//}