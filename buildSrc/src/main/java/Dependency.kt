object Apps {
    const val groupId = "com.github.sieunju"
    const val artifactId = "widget"
    const val libraryName = "widget"
    const val compileSdkVersion = 30
    const val buildToolsVersion = "30.0.3"
    const val minSdkVersion = 21
    const val targetSdkVersion = 30
    const val versionCode = 23
    const val versionName = "1.0.1-beta"
}

object Versions {
    const val kotlin = "1.5.0"
    const val rx = "3.0.0"
    const val lifecycle = "2.3.1"
    const val glide = "4.11.0"
}

object AndroidX {
    const val ktx = "androidx.core:core-ktx:${Versions.kotlin}"
    const val appCompat = "androidx.appcompat:appcompat:1.3.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    const val activity = "androidx.activity:activity-ktx:1.1.0"
    const val material = "com.google.android.material:material:1.4.0"
    const val multidex = "androidx.multidex:multidex:2.0.1"
    const val legacy = "androidx.legacy:legacy-support-v4:1.0.0"
    const val viewpager = "androidx.viewpager2:viewpager2:1.0.0"
    const val cardView = "androidx.cardview:cardview:1.0.0"
    const val palette = "androidx.palette:palette-ktx:1.0.0"
    const val fragment = "androidx.fragment:fragment-ktx:1.3.3"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.2.0"
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
}

object Coroutine {
    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1"
}

object Glide {
    const val base = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val okhttp = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
    const val compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
}

object Rx {
    const val base = "io.reactivex.rxjava3:rxjava:${Versions.rx}"
    const val android = "io.reactivex.rxjava3:rxandroid:${Versions.rx}"
    const val kotlin = "io.reactivex.rxjava3:rxkotlin:${Versions.rx}"
}