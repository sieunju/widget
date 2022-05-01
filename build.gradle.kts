buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

allprojects {
    repositories {
        google()
        maven(url = uri("https://jitpack.io"))
        mavenCentral()
    }
    // group = "com.github.sieunju"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
