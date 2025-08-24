package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
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
        val refresh = view.findViewById<PullToRefreshView>(R.id.vRefresh)
        refresh.setRefreshTriggerDistance(100)
            .setMaxPullDistance(250)
            .setRefreshHeaderHeight(80)
            .setOnRefreshListener(object : PullToRefreshView.Listener {
                override fun onRefresh() {
                    Timber.d("onRefresh!")
                    lifecycleScope.launch {
                        delay(1500)
                        refresh.setRefreshing(false)
                    }
                }

                override fun onPullProgress(progress: Float) {
                    // Timber.d("onPullProgress! ${progress}")
                }
            })

    }

}
