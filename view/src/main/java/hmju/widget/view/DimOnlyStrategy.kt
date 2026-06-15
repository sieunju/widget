package hmju.widget.view

import android.view.View
import androidx.annotation.ColorInt

/**
 * Description : API 31 미만에서 사용되는 graceful degradation 전략.
 *
 * 비트맵 캡처 방식의 실시간 블러는 성능/발열 리스크에 비해 효과가 낮아 사용하지 않고,
 * [mirror] 에 반투명 배경색만 적용해 frosted glass 느낌만 흉내낸다.
 *
 * @param dimColor [mirror] 에 적용할 반투명 배경색 (default `#E6FFFFFF`)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
class DimOnlyStrategy(
    @ColorInt private val dimColor: Int = 0xE6FFFFFF.toInt()
) : BlurStrategy {

    override fun setup(mirror: BlurMirrorView, source: View) {
        mirror.sourceView = null
        mirror.setLayerType(View.LAYER_TYPE_NONE, null)
        mirror.setBackgroundColor(dimColor)
    }

    override fun onScroll() {
        // Blur 미적용 - 별도 갱신 불필요
    }

    override fun release() {
        // 점유 리소스 없음
    }
}
