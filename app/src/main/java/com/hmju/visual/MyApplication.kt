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
			override fun createStackElementTag(element: StackTraceElement): String? {
				return null
			}

			override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
				val newTag = if (tag.isNullOrEmpty()) {
					"Widget"
				} else {
					tag
				}
				super.log(priority, newTag, message, t)
			}
		})
	}

	private fun initTracking() {
		HttpTracking.Builder()
			.setBuildType(true)
			.setWifiShare(true)
			.build(this)
	}
}