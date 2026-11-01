package hmju.widget.view.blur

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

/**
 * Description : API 31(Android 12) 이상에서 [View.setRenderEffect] 와
 * [RenderEffect.createBlurEffect] 를 이용해 실시간 블러를 적용하는 전략.
 *
 * [BlurStyle] 프리셋을 기반으로 균일한 가우시안 블러(iOS UIBlurEffect 와 동일한 방향성)를
 * [blurPasses] 횟수만큼 체이닝한 뒤, 채도(Saturation) 보정과 오버레이 컬러를 적용한다.
 *
 * 블러 갱신은 [onScroll] 호출 시에만 이루어진다.
 * OnPreDrawListener 를 사용하면 mirror 의 invalidate 가 리스너를 재발동시켜
 * 60fps 로 무한 렌더링 루프가 생기기 때문에 사용하지 않는다.
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

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirrorView = mirror
        mirror.setSourceView(source)
        mirror.setRenderEffect(createRenderEffect())
    }

    override fun onScroll() {
        mirrorView?.invalidate()
    }

    override fun release() {
        mirrorView?.setRenderEffect(null)
        mirrorView?.setSourceView(null)
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

//    private fun createRenderEffect(): RenderEffect {
//        var blurEffect: RenderEffect = RenderEffect.createBlurEffect(
//            radius, radius, Shader.TileMode.CLAMP
//        )
//        val colorMatrix = ColorMatrix().apply { setSaturation(saturation) }
//        return RenderEffect.createColorFilterEffect(
//            ColorMatrixColorFilter(colorMatrix), blurEffect
//        )
//    }

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
