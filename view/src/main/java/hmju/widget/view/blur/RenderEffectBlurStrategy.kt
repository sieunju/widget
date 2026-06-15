package hmju.widget.view.blur

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Description : API 31(Android 12) 이상에서 [View.setRenderEffect] 와
 * [RenderEffect.createBlurEffect] 를 이용해 실시간 블러를 적용하는 전략.
 *
 * [mirror] 에 [source] 의 내용을 미러링한 뒤, Blur 와 채도(Saturation) 보정을 체이닝하여
 * iOS frosted glass 와 비슷한 느낌을 낸다.
 *
 * @param radius 블러 반경
 * @param saturation 블러 결과물에 적용할 채도 배율 (1f = 원본, iOS 는 약 1.8배 수준)
 * @param tintAlpha 블러 결과물 위에 덧씌울 흰색 틴트 비율 (0f = 없음, 0.4f = 40%)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
@RequiresApi(Build.VERSION_CODES.S)
class RenderEffectBlurStrategy(
    private var radius: Float = 64f,
    private val saturation: Float = 1.8f,
    private val tintAlpha: Float = 0.4f
) : BlurStrategy {

    private var mirrorView: BlurMirrorView? = null

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirrorView = mirror
        mirror.sourceView = source
        mirror.setRenderEffect(createRenderEffect())
    }

    override fun onScroll() {
        mirrorView?.invalidate()
    }

    override fun release() {
        mirrorView?.setRenderEffect(null)
        mirrorView?.sourceView = null
        mirrorView = null
    }

    override fun setRadius(radius: Float) {
        this.radius = radius
        mirrorView?.setRenderEffect(createRenderEffect())
        mirrorView?.invalidate()
    }

    private fun createRenderEffect(): RenderEffect {
        val blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
        val colorMatrix = ColorMatrix().apply { setSaturation(saturation) }
        colorMatrix.postConcat(createWhiteTintMatrix(tintAlpha))
        return RenderEffect.createColorFilterEffect(
            ColorMatrixColorFilter(colorMatrix), blur
        )
    }

    /**
     * src' = src * (1 - alpha) + 255 * alpha 형태로 흰색을 [alpha] 비율만큼 블렌딩하는 행렬
     */
    private fun createWhiteTintMatrix(alpha: Float): ColorMatrix {
        val scale = 1f - alpha
        val offset = 255f * alpha
        return ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, offset,
                0f, scale, 0f, 0f, offset,
                0f, 0f, scale, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}
