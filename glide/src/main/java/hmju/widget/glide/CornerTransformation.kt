package hmju.widget.glide

import android.graphics.*
import android.os.Build
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import java.security.MessageDigest

/**
 * Description : Corner Transformation
 *
 * Created by juhongmin on 2023/04/10
 */
class CornerTransformation(@CornerType type: Int) : BaseTransformation(type) {

    companion object {
        const val ID = "hmju.widget.glide.transformation.CornerTransformation"
    }

    var cornerRadius = 0
    var borderWidth = -1
    var borderColor = -1

    fun corner(cornerRadius: Int): CornerTransformation {
        this.cornerRadius = cornerRadius
        return this
    }

    fun border(borderWidth: Int, borderColor: Int): CornerTransformation {
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
//        Logger.d("Corner Transform ${toTransform.width} \t${outWidth}")
        val canvasBitmap = pool.emptyBitmap(outWidth, outHeight)
        if (toTransform.width == outWidth && toTransform.height == outHeight) {

            canvasBitmap.switchDensity(toTransform)

            // Draw Round Rect
            drawRoundRect(canvasBitmap, toTransform)

            // Draw Border
            drawBorder(canvasBitmap)

        } else {
            val mergeBitmap = pool.mergeBitmap(
                outWidth,
                outHeight,
                sliceBitmap(borderWidth, outWidth, outHeight, toTransform)
            )

            // Draw Round Rect
            drawRoundRect(canvasBitmap, mergeBitmap)

            // Draw Border
            drawBorder(canvasBitmap)
        }
        return canvasBitmap
    }

    /**
     * Draw Round Rect Func..
     *
     * @param bitmap      Current Bitmap
     * @param toTransform Source Bitmap
     */
    private fun drawRoundRect(bitmap: Bitmap, toTransform: Bitmap) {
        if (cornerRadius == 0) return

        val right = toTransform.width
        val bottom = toTransform.height

        val canvas = Canvas(bitmap).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val rect = RectF()

        // Type All
        if ((type and ALL) == ALL) {
            rect.set(0F, 0F, right.toFloat(), bottom.toFloat())
            canvas.drawRoundRect(rect, cornerRadius.toFloat(), cornerRadius.toFloat(), paint)
        } else {
            val diameter = cornerRadius * 2F // 기울기 길이

            if (type and TOP_LEFT == TOP_LEFT) {
                rect.set(0F, 0F, diameter, diameter)
                canvas.drawArc(rect, 100F, 90F, true, paint) // ┌─
            } else {
                rect.set(0F, 0F, cornerRadius.toFloat(), cornerRadius.toFloat())
                canvas.drawRect(rect, paint) // Top Left ■
            }

            if (type and TOP_RIGHT == TOP_RIGHT) {
                rect.set((right - diameter).toFloat(), 0F, right.toFloat(), diameter.toFloat())
                canvas.drawArc(rect, 270F, 90F, true, paint) // ─┐
            } else {
                rect.set(
                    (right - cornerRadius).toFloat(),
                    0F,
                    right.toFloat(),
                    cornerRadius.toFloat()
                )
                canvas.drawRect(rect, paint) // Top Right ■
            }

            rect.set(
                cornerRadius.toFloat(),
                0F,
                (right - cornerRadius).toFloat(),
                (diameter / 2).toFloat()
            )
            canvas.drawRect(rect, paint) // Top ──

            if (type and BOTTOM_LEFT == BOTTOM_LEFT) {
                rect.set(0F, (bottom - diameter), diameter, bottom.toFloat())
                canvas.drawArc(rect, 90F, 90F, true, paint) // └─
            } else {
                rect.set(
                    0F,
                    (bottom - cornerRadius).toFloat(),
                    cornerRadius.toFloat(),
                    bottom.toFloat()
                )
                canvas.drawRect(rect, paint)  // Bottom Left ■
            }

            if (type and BOTTOM_RIGHT == BOTTOM_RIGHT) {
                rect.set(
                    (right - diameter).toFloat(),
                    (bottom - diameter).toFloat(),
                    right.toFloat(),
                    bottom.toFloat()
                )
                canvas.drawArc(rect, 0F, 90F, true, paint) // ─┘
            } else {
                rect.set(
                    (right - cornerRadius).toFloat(),
                    (bottom - cornerRadius).toFloat(),
                    right.toFloat(),
                    bottom.toFloat()
                )
                canvas.drawRect(rect, paint) // Bottom Right ■
            }

            rect.set(
                cornerRadius.toFloat(),
                (bottom - cornerRadius).toFloat(),
                (right - cornerRadius).toFloat(),
                bottom.toFloat()
            )
            canvas.drawRect(rect, paint) // Bottom ──

            // Body Rect
            rect.set(
                0F,
                cornerRadius.toFloat(),
                right.toFloat(),
                (bottom - cornerRadius).toFloat()
            )
            canvas.drawRect(rect, paint)// Body Rect
        }

        clear(canvas)
    }

    /**
     * Draw Border
     *
     * @param toTransform Source Bitmap
     */
    private fun drawBorder(toTransform: Bitmap) {
        if (borderWidth == -1) return

        val canvas = Canvas(toTransform)
        val path = Path()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
        }

        val rect = RectF()
        val right = toTransform.width
        val bottom = toTransform.height

        // Type ALl
        if (type and ALL == ALL) {
            paint.style = Paint.Style.FILL

            // Draw Inner Rect
            rect.set(
                borderWidth.toFloat(),
                borderWidth.toFloat(),
                (right - borderWidth).toFloat(),
                (bottom - borderWidth).toFloat()
            )
            val innerRadius = cornerRadius - borderWidth / 2F
            path.addRoundRect(
                rect,
                innerRadius,
                innerRadius,
                Path.Direction.CCW
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                canvas.clipOutPath(path)
            } else {
                canvas.clipPath(path, Region.Op.DIFFERENCE)
            }

            // Draw Out Rect
            rect.set(0F, 0F, right.toFloat(), bottom.toFloat())
            path.rewind()
            path.addRoundRect(
                rect,
                cornerRadius.toFloat(),
                cornerRadius.toFloat(),
                Path.Direction.CCW
            )
        } else {
            // Other Type.
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth.toFloat()
            val lineMiddle = borderWidth / 2F

            // Start Path
            path.moveTo(lineMiddle, cornerRadius.toFloat())

            // Round Top Left
            if (type and TOP_LEFT == TOP_LEFT) {
                path.quadTo(
                    lineMiddle,
                    lineMiddle,
                    cornerRadius.toFloat(),
                    lineMiddle
                )
            } else {
                path.lineTo(lineMiddle, lineMiddle)
                path.lineTo(cornerRadius.toFloat(), lineMiddle)
            }

            // Top Line
            path.lineTo((right - cornerRadius).toFloat(), lineMiddle)

            // Round Top Right
            if (type and TOP_RIGHT == TOP_RIGHT) {
                path.quadTo(
                    right - lineMiddle,
                    lineMiddle,
                    right - lineMiddle,
                    cornerRadius.toFloat()
                )
            } else {
                path.lineTo(right - lineMiddle, lineMiddle)
                path.lineTo(right - lineMiddle, cornerRadius.toFloat())
            }

            // Right Line
            path.lineTo(right - lineMiddle, (bottom - cornerRadius).toFloat())

            // Round Bottom Right
            if (type and BOTTOM_RIGHT == BOTTOM_RIGHT) {
                path.quadTo(
                    right - lineMiddle,
                    bottom - lineMiddle,
                    (right - cornerRadius).toFloat(),
                    bottom - lineMiddle
                )
            } else {
                path.lineTo(right - lineMiddle, bottom - lineMiddle)
                path.lineTo((right - cornerRadius).toFloat(), bottom - lineMiddle)
            }

            // Bottom Line
            path.lineTo(cornerRadius.toFloat(), bottom - lineMiddle)

            // Round Bottom Left
            if (type and BOTTOM_LEFT == BOTTOM_LEFT) {
                path.quadTo(
                    lineMiddle,
                    bottom - lineMiddle,
                    lineMiddle,
                    (bottom - cornerRadius).toFloat()
                )
            } else {
                path.lineTo(lineMiddle, bottom - lineMiddle)
                path.lineTo(lineMiddle, (bottom - cornerRadius).toFloat())
            }

            // Left Line
            path.lineTo(lineMiddle, cornerRadius.toFloat())
            path.close()
        }
        canvas.drawPath(path, paint)
        clear(canvas)
    }

    override fun equals(other: Any?): Boolean {
        if (other is CornerTransformation) {
            return type == other.type &&
                    cornerRadius == other.cornerRadius &&
                    borderWidth == other.borderWidth &&
                    borderColor == other.borderColor
        }
        return false
    }

    override fun hashCode(): Int {
        var result = cornerRadius
        result = 31 * result + borderWidth
        result = 31 * result + borderColor
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(
            (ID + cornerRadius + borderWidth + borderColor + type).toByteArray(
                CHARSET
            )
        )
    }
}