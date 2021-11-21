package com.hmju.visual

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import hmju.widget.view.FlexibleImageView

/**
 * Description : 이미지 이동 및 확대가 가능한 뷰 테스트용 Fragment
 *
 * Created by juhongmin on 11/21/21
 */
class FlexibleImageViewFragment : Fragment(R.layout.fragment_flexibleimageview) {

    lateinit var imageView: FlexibleImageView

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.imgThumb)
        Glide.with(imageView)
            .load("https://picsum.photos/id/0/5616/3744")
            .into(imageView)
    }
}