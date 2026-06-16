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
 * [BlurStyle] 프리셋을 기반으로 균일한 가우시안 블러(iOS UIBlurEffect 와 동일한 방향성)를
 * [blurPasses] 횟수만큼 체이닝한 뒤, 채도(Saturation) 보정과 오버레이 컬러를 적용한다.
 *
 * [source] 의 [ViewTreeObserver] 에 [ViewTreeObserver.OnPreDrawListener] 를 등록하여,
 * 스크롤 여부와 관계없이 매 프레임마다 [mirror] 를 갱신해 실시간으로 블러를 반영한다.
 *
 * @param style 블러 스타일 프리셋 ([BlurStyle] 참고)
 * @param blurPasses 블러 패스 횟수 (횟수가 많을수록 더 심하게 번짐, 기본 2)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
@RequiresApi(Build.VERSION_CODES.S)
class RenderEffectBlurStrategy(
    style: BlurStyle = BlurStyle.LIGHT,
    private val blurPasses: Int = 2
) : BlurStrategy {

    private var radius: Float = style.radius
    private var saturation: Float = style.saturation
    private var overlayColor: Int = style.overlayColor

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

    override fun setStyle(style: BlurStyle) {
        this.radius = style.radius
        this.saturation = style.saturation
        this.overlayColor = style.overlayColor
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
        var blurEffect: RenderEffect = RenderEffect.createBlurEffect(
            radius, radius, Shader.TileMode.MIRROR
        )
        repeat(blurPasses - 1) {
            blurEffect = RenderEffect.createChainEffect(
                RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.MIRROR),
                blurEffect
            )
        }
        val colorMatrix = ColorMatrix().apply { setSaturation(saturation) }
        colorMatrix.postConcat(createOverlayColorMatrix(overlayColor))
        return RenderEffect.createColorFilterEffect(
            ColorMatrixColorFilter(colorMatrix), blurEffect
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
