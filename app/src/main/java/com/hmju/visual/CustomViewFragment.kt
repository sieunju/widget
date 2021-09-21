package com.hmju.visual

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class CustomViewFragment : Fragment(R.layout.fragment_custom_view) {

    private val TEMP_URL = "https://memo.qtzz.synology.me/resource/logo.png"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {

            GlobalScope.launch(Dispatchers.Main) {

                findViewById<AppCompatImageView>(R.id.imgThumb)
                    .setImageBitmap(ImageLoader.imageBitmap(TEMP_URL))
            }
        }
    }
}