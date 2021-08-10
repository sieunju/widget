package com.hmju.visual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URL


class CustomViewFragment : Fragment(R.layout.fragment_custom_view) {

    private val TEMP_URL = "https://t1.daumcdn.net/cfile/tistory/2658DF455549EC2425"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            view.findViewById<AppCompatImageView>(R.id.imgThumb)
                .setImageBitmap(imageBitmap(TEMP_URL))
        }
    }

    @Throws(MalformedURLException::class)
    private suspend fun imageBitmap(url: String): Bitmap = withContext(Dispatchers.IO) {
        val bytes = URL(url).readBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}