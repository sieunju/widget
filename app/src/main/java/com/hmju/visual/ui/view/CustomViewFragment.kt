package com.hmju.visual.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hmju.visual.ImageLoader
import com.hmju.visual.R
import hmju.widget.view.CustomTextView
import kotlinx.coroutines.*


class CustomViewFragment : Fragment(R.layout.fragment_custom_view) {

    private val TEMP_URL =
        "https://lh3.googleusercontent.com/S_MBydsRjGbgJDrohpdJlA5ESktGymJrYMftIT3CWYggm86pPSiq26b8P9dwbOI2IYRs"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {

            GlobalScope.launch(Dispatchers.Main) {

                findViewById<AppCompatImageView>(R.id.imgThumb)
                    .setImageBitmap(ImageLoader.imageBitmap(TEMP_URL))
            }

            val tvSample = findViewById<CustomTextView>(R.id.tvSample)
            lifecycleScope.launch(Dispatchers.IO) {
                repeat(30) {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        tvSample.isSelected = !tvSample.isSelected
                    }
                }
            }
        }
    }
}