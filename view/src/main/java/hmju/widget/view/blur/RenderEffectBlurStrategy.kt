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
 * @param radiusX 가로 방향 블러 반경
 * @param radiusY 세로 방향 블러 반경
 * @param saturation 블러 결과물에 적용할 채도 배율 (1f = 원본, iOS 는 약 1.8배 수준)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
@RequiresApi(Build.VERSION_CODES.S)
class RenderEffectBlurStrategy(
    private var radiusX: Float = 22.5f,
    private var radiusY: Float = 22.5f,
    private val saturation: Float = 1.8f
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

    override fun setRadius(radiusX: Float, radiusY: Float) {
        this.radiusX = radiusX
        this.radiusY = radiusY
        mirrorView?.setRenderEffect(createRenderEffect())
        mirrorView?.invalidate()
    }

    private fun createRenderEffect(): RenderEffect {
        val blur = RenderEffect.createBlurEffect(radiusX, radiusY, Shader.TileMode.CLAMP)
        val vibrance = RenderEffect.createColorFilterEffect(
            ColorMatrixColorFilter(ColorMatrix().apply {
                setSaturation(saturation)

            }
            ), blur
        )
        return vibrance
    }
}
