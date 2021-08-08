package com.hmju.visual

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun moveProgress(v : View) {
        startActivity(Intent(this, ProgressViewActivity::class.java))
    }

	fun moveBehavior(v : View){
		startActivity(Intent(this,TranslationBehaviorActivity::class.java))
	}
}