package hmju.widget.view

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Description : API 31(Android 12) 이상에서 [View.setRenderEffect] 와
 * [RenderEffect.createBlurEffect] 를 이용해 실시간 블러를 적용하는 전략.
 *
 * [mirror] 에 [source] 의 내용을 미러링한 뒤, RenderEffect 로 결과물 자체를 흐리게 만든다.
 *
 * @param radiusX 가로 방향 블러 반경
 * @param radiusY 세로 방향 블러 반경
 *
 * Created by juhongmin on 2026. 6. 15.
 */
@RequiresApi(Build.VERSION_CODES.S)
class RenderEffectBlurStrategy(
    private val radiusX: Float = 25f,
    private val radiusY: Float = 25f
) : BlurStrategy {

    private var mirrorView: BlurMirrorView? = null

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirrorView = mirror
        mirror.sourceView = source
        mirror.setRenderEffect(
            RenderEffect.createBlurEffect(radiusX, radiusY, Shader.TileMode.CLAMP)
        )
    }

    override fun onScroll() {
        mirrorView?.invalidate()
    }

    override fun release() {
        mirrorView?.setRenderEffect(null)
        mirrorView?.sourceView = null
        mirrorView = null
    }
}
