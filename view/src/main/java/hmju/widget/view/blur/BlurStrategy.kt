package hmju.widget.view.blur

import android.view.View

/**
 * Description : [BlurMirrorView] 가 뒷배경(RecyclerView 등)을 흐림 처리하는
 * 구체적인 방식을 정의하는 전략 인터페이스.
 *
 * API Level 에 따라 [RenderEffectBlurStrategy] 또는 [DimOnlyStrategy] 로 구현된다.
 *
 * Created by juhongmin on 2026. 6. 15.
 */
interface BlurStrategy {

    /**
     * Blur 를 적용할 [mirror] 와 그 뒤에서 흐려질 대상 [source] 를 연결한다.
     *
     * @param mirror 흐림 효과가 그려질 [BlurMirrorView]
     * @param source 흐림 처리 대상이 되는 View (ex. RecyclerView)
     */
    fun setup(mirror: BlurMirrorView, source: View)

    /**
     * [source] 가 스크롤될 때마다 호출되어 흐림 결과를 갱신한다.
     */
    fun onScroll()

    /**
     * Blur 효과를 해제하고 점유 중인 리소스를 정리한다.
     */
    fun release()

    /**
     * Blur 반경을 실시간으로 변경한다.
     * 반경 개념이 없는 전략(ex. [DimOnlyStrategy])에서는 아무 동작도 하지 않는다.
     *
     * @param radius 블러 반경
     */
    fun setRadius(radius: Float) {}
}
