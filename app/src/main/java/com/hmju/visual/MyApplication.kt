package com.hmju.visual

import android.app.Application
import hmju.http.tracking.HttpTracking
import timber.log.Timber

/**
 * Description :
 *
 * Created by juhongmin on 2022/12/30
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initTracking()
    }

    private fun initTimber() {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                val str = StringBuilder("JLOGGER_")
                try {
                    str.append(
                        element.className
                            .substringAfterLast(".")
                            .substringBefore("$")
                    )
                    str.append(":")
                    str.append(element.methodName.substringAfterLast("."))
                } catch (ex: Exception) {
                    // ignore
                }
                return str.toString()
            }
        })
    }

    private fun initTracking() {
        HttpTracking.Builder()
            .setBuildType(true)
            .build(this)
    }
}