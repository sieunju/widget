package hmju.widget.view.blur

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

/**
 * Description : API 31(Android 12) 이상에서 [View.setRenderEffect] 와
 * [RenderEffect.createBlurEffect] 를 이용해 실시간 블러를 적용하는 전략.
 *
 * [mirror] 에 [source] 의 내용을 미러링한 뒤, Blur 와 채도(Saturation) 보정, 오버레이 컬러를
 * 체이닝하여 iOS frosted glass 와 비슷한 느낌을 낸다.
 *
 * [source] 의 [ViewTreeObserver] 에 [ViewTreeObserver.OnPreDrawListener] 를 등록하여,
 * 스크롤 여부와 관계없이 매 프레임마다 [mirror] 를 갱신해 실시간으로 블러를 반영한다.
 *
 * @param radius 블러 반경
 * @param saturation 블러 결과물에 적용할 채도 배율 (1f = 원본, iOS 는 약 1.8배 수준)
 * @param overlayColor 블러 결과물 위에 덧씌울 오버레이 컬러 (알파 포함, 0x00xxxxxx = 없음)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
@RequiresApi(Build.VERSION_CODES.S)
class RenderEffectBlurStrategy(
    private var radius: Float = 22f,
    private val saturation: Float = 10.0f,
    private var overlayColor: Int = Color.argb(0.6f, 0f, 0f, 0f)
) : BlurStrategy {

    private var mirrorView: BlurMirrorView? = null
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirrorView = mirror
        mirror.sourceView = source
        mirror.setRenderEffect(createRenderEffect())
        registerAutoUpdate(mirror, source)
    }

    override fun onScroll() {
        mirrorView?.invalidate()
    }

    override fun release() {
        unregisterAutoUpdate()
        mirrorView?.setRenderEffect(null)
        mirrorView?.sourceView = null
        mirrorView = null
    }

    override fun setRadius(radius: Float) {
        this.radius = radius
        mirrorView?.setRenderEffect(createRenderEffect())
        mirrorView?.invalidate()
    }

    override fun setOverlayColor(@ColorInt color: Int) {
        this.overlayColor = color
        mirrorView?.setRenderEffect(createRenderEffect())
        mirrorView?.invalidate()
    }

    /**
     * [source] 가 그리는 모든 프레임마다 [mirror] 를 갱신하여,
     * 스크롤뿐만 아니라 이미지 로딩, 애니메이션 등 어떤 변화에도 블러가 실시간으로 반영되도록 한다.
     */
    private fun registerAutoUpdate(mirror: BlurMirrorView, source: View) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            mirror.invalidate()
            true
        }
        source.viewTreeObserver.addOnPreDrawListener(listener)
        preDrawListener = listener
    }

    private fun unregisterAutoUpdate() {
        val listener = preDrawListener ?: return
        mirrorView?.sourceView?.viewTreeObserver?.removeOnPreDrawListener(listener)
        preDrawListener = null
    }

    private fun createRenderEffect(): RenderEffect {
        val blur = RenderEffect.createBlurEffect(radius, 0f, Shader.TileMode.CLAMP)
        val colorMatrix = ColorMatrix().apply { setSaturation(saturation) }
        colorMatrix.postConcat(createOverlayColorMatrix(overlayColor))
        return RenderEffect.createColorFilterEffect(
            ColorMatrixColorFilter(colorMatrix), blur
        )
    }

    /**
     * src' = src * (1 - alpha) + color * alpha 형태로 [color] 를 alpha 비율만큼 블렌딩하는 행렬
     */
    private fun createOverlayColorMatrix(@ColorInt color: Int): ColorMatrix {
        val alpha = Color.alpha(color) / 255f
        val scale = 1f - alpha
        val r = Color.red(color) * alpha
        val g = Color.green(color) * alpha
        val b = Color.blue(color) * alpha
        return ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, r,
                0f, scale, 0f, 0f, g,
                0f, 0f, scale, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}
