import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(Apps.compileSdkVersion)
    buildToolsVersion(Apps.buildToolsVersion)

    defaultConfig {
        applicationId = "com.hmju.visual"
        minSdkVersion(Apps.minSdkVersion)
        targetSdkVersion(Apps.targetSdkVersion)
        versionCode = Apps.versionCode
        versionName = Apps.versionName
        setProperty("archivesBaseName", "Widget-${versionName}")
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = true
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
}

dependencies {
    implementation(project(path = ":widget"))
    implementation(AndroidX.ktx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.material)

    implementation(Coroutine.core)
    implementation(Coroutine.android)

    implementation(Glide.base)
    implementation(Glide.okhttp)
    kapt(Glide.compiler)

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

tasks.register("generateReleaseNote") {
    getReleaseNote()
}

fun getCommand(command: String): String {
    val os = ByteArrayOutputStream()
    exec {
        commandLine = command.split(" ")
        standardOutput = os
    }
    return String(os.toByteArray())
}

/**
 * 마지막 커밋한 메시지 가져와서 릴리즈노트에 입력 하기
 */
fun getReleaseNote() {
    val lastTag = ByteArrayOutputStream().run {
        project.exec {
            commandLine("git describe --tags --abbrev=0".split(" "))
            standardOutput = this@run
        }
        String(this.toByteArray()).trim()
    }

    if (lastTag.isEmpty()) {
        println("Tag Message is Empty!")
        return
    }

    File(project.rootDir.absolutePath, "release_note.txt").run {
        parentFile.mkdir()
        val buildDate = "Build Date ${
            SimpleDateFormat(
                "yyyy년 MM월 dd일 E요일 HH:mm:ss",
                Locale.KOREAN
            ).format(Date())
        }"

        val version = "Version Name: ${Apps.versionName}"
        val branch = "Branch: ${getCommand("git rev-parse --abbrev-ref HEAD")}"
        val msg = "Message: ${getCommand("git rev-list --format=%B --max-count=1 HEAD")}"
        val author = "Author: ${getCommand("git log -1 --pretty=format:%an")}"

        printWriter().use {
            it.println(buildDate)
            it.println(version)
            it.println(branch)
            it.println(msg)
            it.println(author)
        }

        println(buildDate)
        println(version)
        println(branch)
        println(msg)
        println(author)

        println("ReleaseNote Write Success")
    }
}