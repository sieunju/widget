package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hmju.visual.ImageLoader
import com.hmju.visual.R
import hmju.widget.view.CustomLayout
import hmju.widget.view.CustomTextView
import kotlinx.coroutines.*


class CustomViewFragment : Fragment(R.layout.fragment_custom_view) {

    private lateinit var tvChangeStatus : CustomTextView
    private lateinit var clImage : CustomLayout
    private lateinit var ivThumb : AppCompatImageView

    private val TEMP_URL =
        "https://lh3.googleusercontent.com/S_MBydsRjGbgJDrohpdJlA5ESktGymJrYMftIT3CWYggm86pPSiq26b8P9dwbOI2IYRs"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            tvChangeStatus = findViewById(R.id.tvChangeStatus)
            clImage = findViewById(R.id.clImage)
            ivThumb = findViewById(R.id.ivThumb)

            requestTestImage()
            handleTvChangeStatus()
        }
    }

    private fun requestTestImage(){
        lifecycleScope.launch(Dispatchers.Main) {
            ivThumb.setImageBitmap(ImageLoader.imageBitmap(TEMP_URL))
        }
    }

    private fun handleTvChangeStatus(){
        lifecycleScope.launch(Dispatchers.IO) {
            repeat(30) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    tvChangeStatus.isSelected = !tvChangeStatus.isSelected
                }
            }
        }
    }
}