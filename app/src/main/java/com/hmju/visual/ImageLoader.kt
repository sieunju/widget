package com.hmju.visual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.core.graphics.scale

/**
 * Description : 간단한 ImageLoader Class
 *
 * Created by juhongmin on 9/22/21
 */
object ImageLoader {

    // 이미지 캐시 원본 비트맵을 저장.
    private val imageCache: HashMap<String, Bitmap> = HashMap()

    @JvmStatic
    suspend fun reqBitmap(url: String?, width: Int = -1, height: Int = -1): Result<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                if (url.isNullOrEmpty()) {
                    Result.failure(NullPointerException("Url is Null"))
                } else {
                    val bitmap = handleBitmap(url, width, height)
                    if (bitmap != null) {
                        Result.success(bitmap)
                    } else {
                        Result.failure(NullPointerException("Bitmap is Null"))
                    }
                }
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    @JvmStatic
    suspend fun imageBitmap(url: String, width: Int = -1, height: Int = -1): Bitmap? =
        withContext(Dispatchers.IO) {
            imageCache[url]?.let { cacheBitmap ->
                if (width != -1 && height != -1) {
                    if (cacheBitmap.width == width && cacheBitmap.height == height) {
                        return@withContext cacheBitmap
                    } else {
                        return@withContext cacheBitmap.scale(width, height)
                    }
                } else {
                    return@withContext cacheBitmap
                }
            } ?: run {
                val bitmap = requestBitmap(url)
                if (bitmap == null) {
                    return@withContext null
                } else {
                    imageCache[url] = bitmap
                    if (width != -1 && height != -1) {
                        return@withContext bitmap.scale(width, height)
                    } else {
                        return@withContext bitmap
                    }
                }
            }
        }

    private fun requestBitmap(url: String): Bitmap? {
        return try {
            val bytes = URL(url).readBytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (ex: Exception) {
            null
        }
    }

    private fun handleBitmap(url: String, width: Int, height: Int): Bitmap? {
        val cacheBitmap = imageCache[url]
        return if (cacheBitmap != null) {
            if (width != -1 && height != -1) {
                if (cacheBitmap.width == width && cacheBitmap.height == height) {
                    cacheBitmap
                } else {
                    cacheBitmap.scale(width, height)
                }
            } else {
                cacheBitmap
            }
        } else {
            val tempBitmap = requestBitmap(url)
            if (tempBitmap != null) {
                imageCache[url] = tempBitmap
                if (width != -1 && height != -1) {
                    tempBitmap.scale(width, height)
                } else {
                    tempBitmap
                }
            } else {
                null
            }
        }
    }
}