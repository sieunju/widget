package hmju.widget.view

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Description :
 *
 * Created by juhongmin on 2025. 7. 6.
 */
class ShadowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var shadowOffsetX = 0f
    private var shadowOffsetY = 0f
    private var shadowBlur = 0f
    private var shadowColor = Color.TRANSPARENT
    private var cornerRadius = 0f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // 블러 효과를 위해
    }

    fun setShadowProperties(
        offsetX: Float,
        offsetY: Float,
        blur: Float,
        color: Int,
        radius: Float
    ) {
        shadowOffsetX = offsetX
        shadowOffsetY = offsetY
        shadowBlur = blur
        shadowColor = color
        cornerRadius = radius
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBlurredShadow(canvas)
    }

    private fun drawBlurredShadow(canvas: Canvas) {
        if (shadowColor == Color.TRANSPARENT) return

        // 방법 1: BlurMaskFilter 사용
        val shadowPaint = Paint().apply {
            color = shadowColor
            isAntiAlias = true
            maskFilter = BlurMaskFilter(shadowBlur, BlurMaskFilter.Blur.NORMAL)
        }

        val shadowRect = RectF(
            shadowOffsetX,
            shadowOffsetY,
            width.toFloat() - shadowBlur,
            height.toFloat() - shadowBlur
        )

        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)

        // 방법 2: 여러 레이어로 블러 효과 (fallback)
        // drawLayeredShadow(canvas)
    }

    private fun drawLayeredShadow(canvas: Canvas) {
        // BlurMaskFilter가 안 될 때 사용할 방법
        val layers = 12
        val baseAlpha = Color.alpha(shadowColor)
        val red = Color.red(shadowColor)
        val green = Color.green(shadowColor)
        val blue = Color.blue(shadowColor)

        for (i in 0 until layers) {
            val alpha = (baseAlpha * (layers - i) / layers).coerceIn(0, 255)
            val spread = i * (shadowBlur / layers)

            val layerPaint = Paint().apply {
                color = Color.argb(alpha, red, green, blue)
                isAntiAlias = true
            }

            val shadowRect = RectF(
                shadowOffsetX + spread,
                shadowOffsetY + spread,
                width.toFloat() - shadowBlur + spread,
                height.toFloat() - shadowBlur + spread
            )

            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, layerPaint)
        }
    }
}