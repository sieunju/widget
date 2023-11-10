package com.hmju.visual.ui.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.REVERSE
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.RangeSlider
import com.hmju.visual.ExampleThumb
import com.hmju.visual.ImageLoader
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.view.CustomImageView
import hmju.widget.view.CustomLayout
import hmju.widget.view.CustomTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CustomViewFragment : Fragment(R.layout.f_custom_view) {

    private lateinit var tvChangeStatus: CustomTextView
    private lateinit var clImage: CustomLayout
    private lateinit var ivThumb: CustomImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            tvChangeStatus = findViewById(R.id.tvChangeStatus)
            clImage = findViewById(R.id.clImage)
            ivThumb = findViewById(R.id.ivThumb)

            requestTestImage()
            handleTvChangeStatus()
            handleImageCornerAni(ivThumb)

            setMaterialSlider(
                view.findViewById(R.id.rsMaterial),
                view.findViewById(R.id.tvRsMaterialMin),
                view.findViewById(R.id.tvRsMaterialMax)
            )
        }
    }

    private fun requestTestImage() {
        lifecycleScope.launch(Dispatchers.Main) {
            ivThumb.setImageBitmap(ImageLoader.imageBitmap(ExampleThumb.DEEP_LINK_WALLPAPER))
        }
    }

    private fun handleTvChangeStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            repeat(30) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    tvChangeStatus.isSelected = !tvChangeStatus.isSelected
                }
            }
        }
    }

    private fun handleImageCornerAni(view: CustomImageView) {
        ValueAnimator.ofFloat(50F.dp, 0F).apply {
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val corner = it.animatedValue as Float
                view.setCorner(corner)
            }
            repeatCount = 30
            duration = 1500
            startDelay = 3000
            repeatMode = REVERSE
            start()
        }
    }

    private fun setMaterialSlider(
        slider: RangeSlider,
        tvMin: AppCompatTextView,
        tvMax: AppCompatTextView
    ) {
        try {
            val supperClass: Class<in RangeSlider>? = slider.javaClass.superclass
            supperClass?.getDeclaredField("widgetHeight")?.let {
                it.isAccessible = true
                it.set(slider, 25.dp)
            }
            supperClass?.getDeclaredField("labelPadding")?.let {
                it.isAccessible = true
                it.set(slider, 0)
            }
            supperClass?.getDeclaredField("trackTop")?.let {
                it.isAccessible = true
                it.set(slider, 12.dp)
            }
        } catch (ex: Exception) {
            // ignore
        }
        slider.setValues(95F, 95F)
        slider.valueFrom = 95F
        slider.valueTo = 108F
        slider.addOnChangeListener(RangeSlider.OnChangeListener { _, value, fromUser ->
            if (slider.values.size > 1) {
                tvMin.text = "${slider.values[0].toInt()}"
                tvMax.text = "${slider.values[1].toInt()}"
            }
        })
        tvMin.text = "${slider.values[0].toInt()}"
        tvMax.text = "${slider.values[1].toInt()}"
    }
}