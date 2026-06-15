package hmju.widget.view.blur

import android.os.Build

object Extensions {
    /**
     * 디바이스 API Level 에 맞는 [BlurStrategy] 구현체를 반환한다.
     * API 31(Android 12) 이상이면 [RenderEffectBlurStrategy], 미만이면 [DimOnlyStrategy].
     */
    fun create(): BlurStrategy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlurStrategy()
        } else {
            DimOnlyStrategy()
        }
    }
}
