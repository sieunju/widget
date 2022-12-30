package com.hmju.visual

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.hmju.visual.ui.coordinator.TranslationBehaviorFragment
import com.hmju.visual.ui.gesture.FlexibleImageViewFragment
import com.hmju.visual.ui.progress.ProgressFragment
import com.hmju.visual.ui.recyclerview.ParallaxViewHolderFragment
import com.hmju.visual.ui.recyclerview.SpecialGridFragment
import com.hmju.visual.ui.tablayout.CustomTabLayoutFragment
import com.hmju.visual.ui.view.CustomViewFragment
import com.hmju.visual.ui.viewpager.LineIndicatorFragment

/**
 * Description :
 *
 * Created by juhongmin on 8/9/21
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Logger", "onViewCreated")
        with(view) {
            findViewById<Button>(R.id.progress).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, ProgressFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.behavior).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, TranslationBehaviorFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.view).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, CustomViewFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.parallax).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, ParallaxViewHolderFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.flexible).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, FlexibleImageViewFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.tabLayout).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, CustomTabLayoutFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.lineIndicator).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, LineIndicatorFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            findViewById<Button>(R.id.specialGridDecoration).setOnClickListener {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment, SpecialGridFragment())
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }
}
