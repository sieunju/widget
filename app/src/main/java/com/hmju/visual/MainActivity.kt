package com.hmju.visual

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.hmju.visual.ui.select.SelectMenuFragment
import timber.log.Timber
import kotlin.reflect.KClass

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.moveToFragment(SelectMenuFragment::class)
    }

    override fun onBackPressed() {
        Timber.d("Count ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finishAffinity()
        }
    }

    companion object {
        fun FragmentManager.moveToFragment(targetFragment: KClass<out Fragment>) {
            beginTransaction().apply {
                setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                replace(R.id.fragment, targetFragment.java.newInstance())
                addToBackStack(null)
                commit()
            }
        }
    }
}