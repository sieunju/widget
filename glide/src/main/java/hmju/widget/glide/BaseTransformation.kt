package hmju.widget.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.IntDef
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation

/**
 * Description : Glide BaseTransformation
 *
 * Created by juhongmin on 2023/04/10
 */
abstract class BaseTransformation(@CornerType type: Int) : BitmapTransformation() {

    companion object {
        const val TOP_LEFT = 0x00000001
        const val TOP_RIGHT = 1 shl 1
        const val BOTTOM_LEFT = 1 shl 2
        const val BOTTOM_RIGHT = 1 shl 3
        const val ALL = 1 shl 4
    }

    @IntDef(flag = true, value = [TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, ALL])
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class CornerType

    @CornerType
    var type = 0

    init {
        var _type = type
        if (type and TOP_LEFT == TOP_LEFT &&
            type and TOP_RIGHT == TOP_RIGHT &&
            type and BOTTOM_LEFT == BOTTOM_LEFT &&
            type and BOTTOM_RIGHT == BOTTOM_RIGHT
        ) {
            _type = ALL
        }
        this.type = _type
    }

    // Avoids warnings in M+.
    protected open fun clear(canvas: Canvas) {
        canvas.setBitmap(null)
    }

    /**
     * Merge Bitmap
     * @param backWidth 백그라운드 너비
     * @param backHeight 백그라운드 높이
     * @param source Source Bitmap
     * @return Merge Bitmap
     */
    protected fun BitmapPool.mergeBitmap(backWidth: Int, backHeight: Int, source: Bitmap): Bitmap {
        get(backWidth, backHeight, Bitmap.Config.ARGB_8888).let { cacheBitmap ->
            cacheBitmap.setHasAlpha(true)
            cacheBitmap.density = source.density
            // 가운데로 이동
            val moveX = (cacheBitmap.width - source.width) / 2F
            val moveY = (cacheBitmap.height - source.height) / 2F
            val canvas = Canvas(cacheBitmap)
            canvas.drawBitmap(cacheBitmap, 0F, 0F, null)
            canvas.drawBitmap(source, moveX, moveY, null)
            // Cache Save
//            put(source)
            return cacheBitmap
        }
    }

    /**
     * Bitmap 자르기 함수
     * @param margin 한쪽면 여백
     * @param source Source Bitmap
     */
    protected fun sliceBitmap(margin: Int, outWidth: Int, outHeight: Int, source: Bitmap): Bitmap {
        if (margin == -1) return source
        // Bitmap 을 자르지 않아도 되는 경우.
        if (outWidth > source.width + (margin.toFloat() * 2F) &&
            outHeight > source.height + (margin.toFloat() * 2F)
        ) {
            return source
        }

        val sliceWidth = source.width - (margin.toFloat() * 2F)
        val sliceHeight = (sliceWidth * source.height.toFloat()) / source.width.toFloat()
        return Bitmap.createScaledBitmap(source, sliceWidth.toInt(), sliceHeight.toInt(), true)
    }

    /**
     * Cache Bitmap Getter
     * @param width Cache Width
     * @param height Cache Height
     * @return Empty Bitmap
     */
    protected fun BitmapPool.emptyBitmap(width: Int, height: Int): Bitmap =
        get(width, height, Bitmap.Config.ARGB_8888).apply { setHasAlpha(true) }

    protected fun Bitmap.switchDensity(sourceBitmap: Bitmap) {
        density = sourceBitmap.density
    }
}