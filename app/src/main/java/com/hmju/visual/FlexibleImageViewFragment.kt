package com.hmju.visual

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import hmju.widget.extensions.captureBitmap
import hmju.widget.view.FlexibleImageView

/**
 * Description : 이미지 이동 및 확대가 가능한 뷰 테스트용 Fragment
 *
 * Created by juhongmin on 11/21/21
 */
class FlexibleImageViewFragment : Fragment(R.layout.fragment_flexibleimageview) {

	lateinit var imageView: FlexibleImageView
	lateinit var flexibleCaptureView: ConstraintLayout

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
		flexibleCaptureView = view.findViewById(R.id.clFlexible)
		var imageUrl = "content://media/external/images/media/439"
		imageUrl = "https://image.zdnet.co.kr/2021/08/27/48a2291e7cbed1be50aa28880b58477e.jpg"
		imageView.loadUrl(imageUrl)

		view.findViewById<Button>(R.id.cropButton).setOnClickListener {
			imageView.centerCrop()
		}
		view.findViewById<Button>(R.id.fitButton).setOnClickListener {
			imageView.fitCenter()
		}
		view.findViewById<Button>(R.id.blurButton).setOnClickListener {

		}
		view.findViewById<Button>(R.id.captureButton).setOnClickListener {
			flexibleCaptureView.captureBitmap {
				view.findViewById<AppCompatImageView>(R.id.imgCapture).setImageBitmap(it)
			}
		}
	}
}