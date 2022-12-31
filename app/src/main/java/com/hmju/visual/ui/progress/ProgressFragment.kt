package com.hmju.visual.ui.progress

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnCancel
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import com.hmju.visual.R
import hmju.widget.progress.ProgressView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ProgressFragment : Fragment(R.layout.f_progress) {

    private lateinit var normalProgress: ProgressView
    private lateinit var fastAndSlowProgress: ProgressView
    private var fastAndSlowAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            normalProgress = findViewById(R.id.normalProgress)
            fastAndSlowProgress = findViewById(R.id.fastAndSlowProgress)

            findViewById<AppCompatTextView>(R.id.tvStart).setOnClickListener {
                startProgressAni()
            }
            findViewById<AppCompatTextView>(R.id.tvInit).setOnClickListener {
                resetProgress()
            }
        }

    }

    private fun startProgressAni() {

        lifecycleScope.launch(Dispatchers.Default) {
            for (idx in 0 until 1000) {
                if (idx % 100 == 0) {
                    normalProgress.currentProgress = 1
                } else {
                    normalProgress.incrementProgressBy(1)
                }
                delay(30)
            }
        }

        fastAndSlowAnimator = ObjectAnimator.ofInt(0, 101).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = -1
            doOnCancel {
                fastAndSlowProgress.currentProgress = 1
            }
            addUpdateListener {
                fastAndSlowProgress.currentProgress = it.animatedValue as Int
            }
            start()
        }
    }

    private fun resetProgress() {
        normalProgress.currentProgress = 1
        fastAndSlowAnimator?.cancel()
        fastAndSlowAnimator = null
    }
}