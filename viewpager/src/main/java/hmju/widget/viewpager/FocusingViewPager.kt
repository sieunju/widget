package hmju.widget.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Description : ChildView -> ViewPager2
 * 간단하게 ViewPager2 에 터치를 훔칠지 안할지 처리하는 함수
 *
 * Created by juhongmin on 2022/12/27
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class FocusingViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @FloatRange(from = 0.0, to = 90.0)
    var leftHorizontalDegree: Double = 45.0

    @FloatRange(from = 90.0, to = 180.0)
    var rightHorizontalDegree: Double = 135.0 // Horizontal Degree

    @FloatRange(from = 0.0, to = 25.0)
    var startVerticalDegree: Double = 0.0

    @FloatRange(from = 25.0, to = 45.0)
    var endVerticalDegree: Double = 45.0 // Vertical Degree

    var diffLocationGap: Float = 30F // 해당 차이값에 따라서 이전 좌표를 저장유무를 판단 한다.

    private var prevX = -1F
    private var prevY = -1F

    private var childViewPager: ViewPager2? = null
        get() {
            if (field == null) {
                field = getChildAt(0) as ViewPager2
            }
            return field
        }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            when (ev.action) {
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    prevX = 0F
                    prevY = 0F
                }
                MotionEvent.ACTION_DOWN -> {
                    prevX = ev.x
                    prevY = ev.y
                }
                MotionEvent.ACTION_MOVE -> {
                    performFocusing(prevX, ev.x, prevY, ev.y)
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun performFocusing(prevX: Float, currX: Float, prevY: Float, currY: Float) {
        if (childViewPager?.adapter == null) return

        childViewPager?.runCatching {
            if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (isHorizontalSwipe(prevX, currX, prevY, currY)) {
                    val currentPos = currentItem
                    val itemCount = adapter?.itemCount ?: 0
                    val lastPos = itemCount.minus(1).coerceAtLeast(0)
                    val diffX = currX - prevX

                    // 좌 -> 우
                    if (diffX > 0) {
                        if (currentPos == 0) {
                            this.requestDisallowInterceptTouchEvent(false)
                        } else {
                            this.requestDisallowInterceptTouchEvent(true)
                        }
                    } else {
                        // 우 -> 좌
                        if (currentPos == lastPos) {
                            this.requestDisallowInterceptTouchEvent(false)
                        } else {
                            this.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
            } else if (orientation == ViewPager2.ORIENTATION_VERTICAL) {
                // 수직 ViewPager2 은 나중에 처리할 예정
                // ignore
            }
        }
    }

    /**
     * 제스처가 수평으로 하는 제스처인지 계산 처리
     * @param prevX 이전 X 좌표
     * @param currX 현재 터치한 X 좌표
     * @param prevY 이전 Y 좌표
     * @param currY 현재 터치한 Y 좌표
     * @return true 수평 제스처 인경우, false 수직 제스처 인경우
     */
    private fun isHorizontalSwipe(prevX: Float, currX: Float, prevY: Float, currY: Float): Boolean {
        val deltaX = prevX - currX
        val deltaY = prevY - currY
        // 두점의 각도를 구함
        var result = Math.toDegrees(atan2(deltaY, deltaX).toDouble())
        result = abs(result)
        // 수평 제스처 기준 0~45, 135~180
        return if (0.0 < result && result < leftHorizontalDegree) {
            true
        } else rightHorizontalDegree < result && result < 180.0
    }
}
