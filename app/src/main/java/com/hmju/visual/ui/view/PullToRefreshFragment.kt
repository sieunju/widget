package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager = Glide.with(this)
        val llRefresh = view.findViewById<LinearLayoutCompat>(R.id.llRefresh)
        val refresh = view.findViewById<PullToRefreshView>(R.id.vRefresh)

        refresh
            .setMaxPullDistance(240)
            .setRefreshHeaderHeight(80)
            .setListener(object : PullToRefreshView.Listener {
                override fun onRefresh() {
                    Timber.d("onRefresh!")
                    lifecycleScope.launch {
                        delay(1500)
                        refresh.setRefreshing(false)
                        refresh.addView(View(context))
                    }
                }

                override fun onPullProgress(progress: Float) {
                    Timber.d("onPullProgress! ${progress}")
                    llRefresh.alpha = progress
                }
            })

        lifecycleScope.launch {
            delay(2000)
            refresh.setRefreshing(true)
        }
    }

}
