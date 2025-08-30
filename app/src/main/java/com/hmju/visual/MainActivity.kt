package com.hmju.visual

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.hmju.visual.ui.select.SelectMenuFragment
import kotlin.reflect.KClass

internal class MainActivity : AppCompatActivity() {

    private lateinit var onBackPressCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)
        onBackPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    finishAffinity()
                }
            }
        }

        supportFragmentManager.moveToFragment(SelectMenuFragment::class)
        onBackPressedDispatcher.addCallback(this, onBackPressCallback)
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
                replace(R.id.fragment, targetFragment.java.getDeclaredConstructor().newInstance())
                addToBackStack(null)
                commit()
            }
        }
    }
}