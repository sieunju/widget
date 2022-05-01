buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = uri("https://jitpack.io"))
    }
    group = "com.github.sieunju"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
