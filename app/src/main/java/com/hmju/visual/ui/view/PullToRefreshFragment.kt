package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.R

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
    }

}
