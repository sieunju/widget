package com.hmju.visual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import hmju.widget.view.CustomTextView
import kotlinx.coroutines.*
import java.net.MalformedURLException
import java.net.URL


class CustomViewFragment : Fragment(R.layout.fragment_custom_view) {

    private val TEMP_URL = "https://memo.qtzz.synology.me/resource/logo.png"
    private val TEMP_STR = """
        Ui 에서 실시간으로 진행률을 나타내야 할때 UiThread 로 처리하기에는 어려움이 있습니다.
        그래서 주로 카메라 프리뷰에서 사용되는 SurfaceView 기반의 ProgressView 를 만들었습니다.
        (UiThread 가 아닌 Worker Thread 에서 사용하셔도 Ui 표현이 가능합니다.)
    """.trimIndent()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            view.findViewById<AppCompatImageView>(R.id.imgThumb)
                .setImageBitmap(imageBitmap(TEMP_URL))
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(3000)
            view.findViewById<CustomTextView>(R.id.tvAuto).text = TEMP_STR
        }
    }

    @Throws(MalformedURLException::class)
    private suspend fun imageBitmap(url: String): Bitmap = withContext(Dispatchers.IO) {
        val bytes = URL(url).readBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}