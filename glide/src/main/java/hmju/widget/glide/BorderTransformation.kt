package hmju.widget.glide

import android.graphics.*
import android.os.Build
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import java.security.MessageDigest

/**
 * Description : Corner And Border Transformation
 *
 * Created by juhongmin on 2023/04/10
 */
class BorderTransformation : BaseTransformation(ALL) {

    companion object {
        const val ID = "hmju.widget.glide.transformation.BorderTransformation"
    }

    var borderWidth = - 1
    var borderColor = - 1

    fun border(borderWidth: Int, borderColor: Int): BorderTransformation {
        this.borderWidth = borderWidth
        this.borderColor = borderColor
        return this
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // Draw Border
        if (toTransform.width == outWidth && toTransform.height == outHeight) {
            drawBorder(toTransform)
            return toTransform
        } else {
            val sliceBitmap = sliceBitmap(borderWidth, outWidth, outHeight, toTransform)
            val mergeBitmap = pool.mergeBitmap(outWidth, outHeight, sliceBitmap)
            drawBorder(mergeBitmap)
            return mergeBitmap
        }
    }

    @Suppress("DEPRECATION")
    private fun drawBorder(source: Bitmap) {
        if (borderWidth == - 1) return

        val canvas = Canvas(source)
        val path = Path()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.FILL
        }
        val rect = RectF()
        val right = source.width
        val bottom = source.height

        // Draw Inner Rect
        rect.set(
            borderWidth.toFloat(),
            borderWidth.toFloat(),
            (right - borderWidth).toFloat(),
            (bottom - borderWidth).toFloat()
        )

        path.addRect(rect, Path.Direction.CCW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipOutPath(path)
        } else {
            canvas.clipPath(path, Region.Op.DIFFERENCE)
        }

        // Draw Out Rect
        rect.set(0F, 0F, right.toFloat(), bottom.toFloat())
        path.rewind()
        path.addRect(
            rect,
            Path.Direction.CCW
        )
        canvas.drawPath(path, paint)
        clear(canvas)
    }

    override fun equals(other: Any?): Boolean {
        if (other is BorderTransformation) {
            return type == other.type &&
                    borderWidth == other.borderWidth &&
                    borderColor == other.borderColor
        }
        return false
    }

    override fun hashCode(): Int {
        var result = borderWidth
        result = 31 * result + borderColor
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + borderWidth + borderColor + type).toByteArray(CHARSET))
    }
}