package com.hmju.visual

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Description : 간단한 ImageLoader Class
 *
 * Created by juhongmin on 9/22/21
 */
object ImageLoader {

	// 이미지 캐시 원본 비트맵을 저장.
	private val imageCache: HashMap<String, Bitmap> by lazy { HashMap() }

	@JvmStatic
	suspend fun imageBitmap(url: String, width: Int = -1, height: Int = -1): Bitmap? = withContext(Dispatchers.IO) {
		imageCache[url]?.let { cacheBitmap ->
			if (width != -1 && height != -1) {
				if (cacheBitmap.width == width && cacheBitmap.height == height) {
					return@withContext cacheBitmap
				} else {
					return@withContext Bitmap.createScaledBitmap(cacheBitmap, width, height, true)
				}
			} else {
				return@withContext cacheBitmap
			}
		} ?: run {
			val bitmap = requestBitmap(url)
			if (bitmap == null) {
				return@withContext null
			} else {
				imageCache.put(url, bitmap)
				if (width != -1 && height != -1) {
					return@withContext Bitmap.createScaledBitmap(bitmap, width, height, true)
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
}