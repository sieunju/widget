package com.hmju.visual

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment, MainFragment())
            addToBackStack(null)
            commit()
        }
    }

    override fun onBackPressed() {
        Log.d("Logger","Count ${supportFragmentManager.backStackEntryCount}")
        if(supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finishAffinity()
        }
    }
}