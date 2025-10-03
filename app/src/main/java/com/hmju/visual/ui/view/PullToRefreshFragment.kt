package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.R
import hmju.widget.view.PullToRefreshView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Description :
 *
 * Created by juhongmin on 2025. 8. 24.
 */
internal class PullToRefreshFragment : Fragment(R.layout.f_pull_to_refresh) {

    private lateinit var requestManager: RequestManager

    private val lottieList = listOf(
        R.raw.lottie_example_1,
        R.raw.lottie_example_2,
        R.raw.lottie_example_3,
        R.raw.lottie_example_4
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager = Glide.with(this)
        val refresh = view.findViewById<PullToRefreshView>(R.id.vRefresh)
        val clRefreshHeader = view.findViewById<ConstraintLayout>(R.id.clRefreshHeader)
        val vBgRefresh = view.findViewById<LottieAnimationView>(R.id.vBgRefresh)
        refresh
            .setScrollType(PullToRefreshView.ScrollType.TRANSLATION)
            .setRefreshHeaderHeight(140)
            .setTriggerDistance(140)
            .setMaxPullDistance(160)
            .setListener(object : PullToRefreshView.Listener {
                override fun onRefresh() {
                    Timber.d("onRefresh!")
                    lifecycleScope.launch {
                        delay(500)
                        handleRandomLottie(vBgRefresh)
                        refresh.setRefresh(false)
                        delay(500)
                    }
                }

                override fun onPullProgress(progress: Float) {
                    Timber.d("onPullProgress! ${progress}")
                    if (progress == 0f) {
                        vBgRefresh.pauseAnimation()
                    } else if (!vBgRefresh.isAnimating) {
                        vBgRefresh.playAnimation()
                    }

                    vBgRefresh.alpha = progress
                    clRefreshHeader.alpha = progress
                    val scale = 0.8f + (progress * 0.2f)
                    vBgRefresh.scaleX = scale
                    vBgRefresh.scaleY = scale
                }
            })
        handleRandomLottie(vBgRefresh)
        vBgRefresh.addLottieOnCompositionLoadedListener {
            Timber.d("BG Lottie ${it}")
        }
    }

    private fun handleRandomLottie(bg: LottieAnimationView) {
        bg.cancelAnimation()
        bg.setAnimation(lottieList.random())
        bg.alpha = 0f
        bg.scaleX = 0.8f
        bg.scaleY = 0.8f
    }

}
