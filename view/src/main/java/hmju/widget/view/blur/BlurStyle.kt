package hmju.widget.view.blur

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Description : iOS UIBlurEffectStyle 에 대응하는 블러 프리셋.
 *
 * 각 스타일은 블러 반경(radius), 채도 배율(saturation), 오버레이 컬러(overlayColor) 조합으로
 * iOS 와 유사한 frosted glass 느낌을 낸다.
 *
 * Created by juhongmin on 2026. 6. 16.
 */
enum class BlurStyle(
    val radius: Float,
    val saturation: Float,
    @ColorInt val overlayColor: Int
) {
    /** UIBlurEffectStyle.light 대응 — 균일 블러 + 흰색 50% 오버레이 */
    LIGHT(25f, 1.8f, Color.argb(128, 255, 255, 255)),

    /** UIBlurEffectStyle.extraLight 대응 — 균일 블러 + 흰색 70% 오버레이 */
    EXTRA_LIGHT(22f, 2.0f, Color.argb(179, 255, 255, 255)),

    /** UIBlurEffectStyle.dark 대응 — 균일 블러 + 검정 40% 오버레이 */
    DARK(22f, 1.5f, Color.argb(102, 0, 0, 0)),

    /** UIBlurEffectStyle.prominent 대응 — 강한 블러 + 높은 채도 */
    PROMINENT(30f, 2.2f, Color.argb(128, 255, 255, 255))
}
