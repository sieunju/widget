import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
    group = "com.github.sieunju"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
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
    println("여기여기 $project")
    val lastTag = ByteArrayOutputStream().run {
        println("11111111111111122222222")
        project.exec {
            commandLine("git describe --tags --abbrev=0".split(" "))
            standardOutput = this@run
        }
        String(this.toByteArray()).trim()
    }
    println("여기까지왔어!!! ")
    if (lastTag.isEmpty()) {
        println("Tag Message is Empty!")
        return
    }

    File(project.rootDir.absolutePath.plus("/appRelease"), "release_note.txt").run {
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