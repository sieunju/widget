package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
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

    private val refreshText = listOf("당겨서 새로고침", "당신의 맞춤추첨", "이번에 갱신?")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager = Glide.with(this)
        val llRefresh = view.findViewById<LinearLayoutCompat>(R.id.llRefresh)
        val refresh = view.findViewById<PullToRefreshView>(R.id.vRefresh)
        val tvTitle = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        tvTitle.text = refreshText.random()
        refresh
            .setTriggerDistance((100 + 10).toInt())
            .setRefreshHeaderHeight(100)
            .setListener(object : PullToRefreshView.Listener {
                override fun onRefresh() {
                    Timber.d("onRefresh!")
                    lifecycleScope.launch {
                        delay(500)
                        refresh.setRefresh(false)
                        tvTitle.text = refreshText.random()
                    }
                }

                override fun onPullProgress(progress: Float) {
                    Timber.d("onPullProgress! ${progress}")
                    llRefresh.alpha = progress
                }
            })
    }

}
