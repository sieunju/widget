@file:Suppress("DEPRECATION")

package hmju.widget.view.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withScale

/**
 * Description : API 31 미만에서 사용하는 RenderScript 블러 전략.
 *
 * source 와 [BlurMirrorView.backgroundView] 를 [downscaleFactor] 배 축소해 캡처한 뒤
 * [ScriptIntrinsicBlur] 로 블러를 적용하고 원본 크기로 bilinear 업스케일한다.
 *
 * [ScriptIntrinsicBlur.setRadius] 의 최대값이 25f 이므로, 더 강한 블러는
 * [downscaleFactor] 를 높여 작은 비트맵에서 연산하는 방식으로 구현한다.
 *
 * API 31 이상에서는 [RenderEffectBlurStrategy] 를 사용할 것.
 *
 * @param style 블러 스타일 (overlayColor 만 적용됨)
 * @param downscaleFactor 캡처 해상도를 낮추는 배율 (기본 4)
 * @param blurRadius RS 블러 반경 0f~25f (기본 15f)
 *
 * Created by juhongmin on 2026. 6. 24.
 */
@Suppress("DEPRECATION")
class SoftwareBlurStrategy(
    style: BlurStyle = BlurStyle.LIGHT,
    private val downscaleFactor: Int = 4,
    private val blurRadius: Float = 15f
) : BlurStrategy {

    @ColorInt
    private var overlayColor: Int = style.overlayColor

    private var mirrorView: BlurMirrorView? = null
    private var sourceRef: View? = null
    private var blurDrawable: UpdateableBitmapDrawable? = null
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private var rs: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null

    private val dstLocation = IntArray(2)
    private val srcLocation = IntArray(2)

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirrorView = mirror
        sourceRef = source
        rs = RenderScript.create(mirror.context).also { renderScript ->
            blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).apply {
                setRadius(blurRadius.coerceIn(0f, 25f))
            }
        }
        mirror.setSourceView(null)
        mirror.setLayerType(View.LAYER_TYPE_NONE, null)
        val drawable = UpdateableBitmapDrawable()
        blurDrawable = drawable
        mirror.background = drawable
        registerAutoUpdate(source)
    }

    override fun onScroll() = Unit

    override fun release() {
        unregisterAutoUpdate()
        blurDrawable?.recycle()
        blurDrawable = null
        mirrorView?.background = null
        mirrorView = null
        sourceRef = null
        blurScript?.destroy()
        rs?.destroy()
        blurScript = null
        rs = null
    }

    override fun setOverlayColor(@ColorInt color: Int) {
        overlayColor = color
    }

    override fun setStyle(style: BlurStyle) {
        overlayColor = style.overlayColor
    }

    private fun registerAutoUpdate(source: View) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            updateBlur()
            true
        }
        source.viewTreeObserver.addOnPreDrawListener(listener)
        preDrawListener = listener
    }

    private fun unregisterAutoUpdate() {
        val listener = preDrawListener ?: return
        sourceRef?.viewTreeObserver?.removeOnPreDrawListener(listener)
        preDrawListener = null
    }

    private fun updateBlur() {
        val mirror = mirrorView ?: return
        val source = sourceRef ?: return
        if (mirror.width == 0 || mirror.height == 0) return

        val scale = 1f / downscaleFactor
        val smallW = (mirror.width / downscaleFactor).coerceAtLeast(1)
        val smallH = (mirror.height / downscaleFactor).coerceAtLeast(1)
        val small = createBitmap(smallW, smallH)
        val smallCanvas = Canvas(small)

        mirror.getLocationInWindow(dstLocation)

        mirror.backgroundView?.let { bg ->
            if (bg.width > 0 && bg.height > 0) {
                bg.getLocationInWindow(srcLocation)
                smallCanvas.withScale(scale, scale) {
                    translate(
                        (srcLocation[0] - dstLocation[0]).toFloat(),
                        (srcLocation[1] - dstLocation[1]).toFloat()
                    )
                    bg.draw(this)
                }
            }
        }

        if (source.width > 0 && source.height > 0) {
            source.getLocationInWindow(srcLocation)
            smallCanvas.withScale(scale, scale) {
                translate(
                    (srcLocation[0] - dstLocation[0]).toFloat(),
                    (srcLocation[1] - dstLocation[1]).toFloat()
                )
                source.draw(this)
            }
        }

        applyRenderScriptBlur(small)

        if (Color.alpha(overlayColor) > 0) {
            smallCanvas.drawColor(overlayColor)
        }

        val blurred = small.scale(mirror.width, mirror.height)
        small.recycle()
        blurDrawable?.update(blurred)
    }

    private fun applyRenderScriptBlur(bitmap: Bitmap) {
        val script = blurScript ?: return
        val renderScript = rs ?: return
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createTyped(renderScript, input.type)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        input.destroy()
        output.destroy()
    }

    private class UpdateableBitmapDrawable : Drawable() {
        private var bitmap: Bitmap? = null
        private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        fun update(newBitmap: Bitmap) {
            val old = bitmap
            bitmap = newBitmap
            old?.recycle()
            invalidateSelf()
        }

        fun recycle() {
            bitmap?.recycle()
            bitmap = null
        }

        override fun draw(canvas: Canvas) {
            bitmap?.let { canvas.drawBitmap(it, null, bounds, paint) }
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
