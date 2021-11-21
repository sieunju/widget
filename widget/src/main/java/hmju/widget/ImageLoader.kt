package hmju.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

/**
 * Description : 간단한 이미지 로더 Object Class
 *
 * Created by juhongmin on 11/21/21
 */
internal object ImageLoader {

    /**
     * Load Bitmap Http 통신으로 받아 오는 타입
     * @param url Load Url
     */
    @JvmStatic
    suspend fun loadBitmapHttp(url: String?): Bitmap? {
        if (url == null) return null
        return withContext(Dispatchers.IO) {
            requestBitmap(url)
        }
    }

    /**
     * Request Bitmap
     */
    private fun requestBitmap(url: String): Bitmap? {
        return try {
            val bytes = URL(url).readBytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (ex: IOException) {
            null
        }
    }

    @JvmStatic
    suspend fun loadBitmapFile(context: Context, url: String?): Bitmap? {
        if (url == null) return null
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return withContext(Dispatchers.IO) {
            requestBitmapFile(context, url)
        }
    }

    @Suppress("DEPRECATION")
    private fun requestBitmapFile(context: Context, url: String): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        Uri.parse(url)
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(url))
            }
        } catch (ex: Exception) {
            null
        }
    }
}