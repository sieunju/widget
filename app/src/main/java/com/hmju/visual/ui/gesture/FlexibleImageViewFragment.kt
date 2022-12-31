package com.hmju.visual.ui.gesture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hmju.visual.ExampleThumb
import com.hmju.visual.ImageLoader
import com.hmju.visual.R
import hmju.widget.gesture.FlexibleImageEditView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Description : 이미지 이동 및 확대가 가능한 뷰 테스트용 Fragment
 *
 * Created by juhongmin on 11/21/21
 */
internal class FlexibleImageViewFragment : Fragment(R.layout.fragment_flexibleimageview) {

    lateinit var edit: FlexibleImageEditView
    lateinit var editFrame: FrameLayout
    lateinit var ivCapture: AppCompatImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            edit = findViewById(R.id.edit)
            editFrame = findViewById(R.id.frame)
            ivCapture = findViewById(R.id.ivCapture)

            findViewById<Button>(R.id.bFitAndCrop).setOnClickListener {
                val state = edit.getFlexibleStateItem()
                if (state.scale == state.minScale) {
                    edit.centerCrop()
                } else {
                    edit.fitCenter()
                }
            }

            findViewById<Button>(R.id.captureButton).setOnClickListener {
                handleCapture()
            }

            handleImageThumb()
        }
    }

    private fun handleImageThumb() {
        lifecycleScope.launch(Dispatchers.Main) {
            ImageLoader.reqBitmap(ExampleThumb.GALAXY)
                .onSuccess { edit.loadBitmap(it) }
        }
    }

    private fun handleCapture() {
        lifecycleScope.launch(Dispatchers.Main) {
            val rect = edit.getStateItem()
            if (rect != null) {
                getFlexibleBitmap(
                    edit.getImageBitmap(),
                    rect,
                    edit
                ).onSuccess { ivCapture.setImageBitmap(it) }
            }
        }
    }

    private suspend fun getFlexibleBitmap(
        originalBitmap: Bitmap,
        srcRect: RectF,
        edit: FlexibleImageEditView
    ): Result<Bitmap> {
        return withContext(Dispatchers.Main) {
            try {
                val status = edit.getFlexibleStateItem()
                val bitmap = Bitmap.createBitmap(
                    status.viewWidth,
                    status.viewHeight,
                    Bitmap.Config.ARGB_8888
                )
                val tempBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    srcRect.width().toInt(),
                    srcRect.height().toInt(),
                    true
                )
                Canvas(bitmap).apply {
                    drawColor(Color.WHITE)
                    drawBitmap(tempBitmap, null, srcRect, null)
                }
                Result.success(bitmap)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }
}