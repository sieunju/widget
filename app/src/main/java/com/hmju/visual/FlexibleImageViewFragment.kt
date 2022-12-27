package com.hmju.visual

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import hmju.widget.extensions.backgroundCaptureBitmap
import hmju.widget.gesture.FlexibleImageEditView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : 이미지 이동 및 확대가 가능한 뷰 테스트용 Fragment
 *
 * Created by juhongmin on 11/21/21
 */
class FlexibleImageViewFragment : Fragment(R.layout.fragment_flexibleimageview) {

    lateinit var imageView: FlexibleImageEditView
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
        // imageUrl = "content://media/external/images/media/6113"
        Glide.with(requireContext())
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap?>() {

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    imageView.loadBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("JLogger", "Error Bitmap ${placeholder}")
                }
            })

        // imageView.loadUrl(imageUrl)

        view.findViewById<Button>(R.id.cropButton).setOnClickListener {
            imageView.centerCrop()
        }
        view.findViewById<Button>(R.id.fitButton).setOnClickListener {
            imageView.fitCenter()
        }
        view.findViewById<Button>(R.id.blurButton).setOnClickListener {

        }
        view.findViewById<Button>(R.id.captureButton).setOnClickListener {
            val stateItem = imageView.getStateItem() ?: return@setOnClickListener

            Glide.with(requireContext())
                .asDrawable()
                .load(imageUrl)
                .into(object : CustomTarget<Drawable?>() {

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        val bitmapDrawable = resource as BitmapDrawable
                        GlobalScope.launch {
                            val bitmap = withContext(Dispatchers.IO) {
                                backgroundCaptureBitmap(
                                    bitmapDrawable.bitmap,
                                    stateItem,
                                    flexibleCaptureView.width,
                                    flexibleCaptureView.height
                                )
                            }
                            withContext(Dispatchers.Main) {
                                view.findViewById<AppCompatImageView>(R.id.imgCapture)
                                    .setImageBitmap(bitmap)
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}