package hmju.widget.recyclerview.scroller

import android.content.Context
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

/**
 * Description : RecyclerView Smooth Scroller
 * @see [LinearSmoothScroller] 에서 포커싱을 중간으로 하는것과 애니메이션 효과만 커스텀화 한
 * 컨트롤러
 * 수평 or 수직 스크롤 지원
 *
 * Created by juhongmin on 2022/02/03
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomLinearScroller(context: Context) : LinearSmoothScroller(context) {

    enum class ScrollerType {
        START, END, CENTER
    }

    companion object {
        const val TARGET_SEEK_SCROLL_DISTANCE_PX = 10000
        const val TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2F
    }

    var type: ScrollerType = ScrollerType.START
    var scrollOffset: Int = 0
    var interceptor: Interpolator = FastOutSlowInInterpolator()
    var duration: Int = 400

    override fun getHorizontalSnapPreference(): Int {
        // Scroll 하고자 하는 뷰의 Left 에서 되도록
        return if (type == ScrollerType.START) {
            SNAP_TO_START
        } else {
            SNAP_TO_END
        }
    }

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        var offset: Int
        when (type) {
            ScrollerType.START -> {
                offset = boxStart - viewStart
                offset += scrollOffset
            }
            ScrollerType.END -> {
                offset = boxEnd - viewEnd
                offset -= scrollOffset
            }
            else -> {
                // Center
                offset =
                    (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
            }
        }
        return offset
    }

    /**
     * 바로 옆에서 포지션을 이동하는 경우 아래 함수 호출
     */
    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val dx = calculateDxToMakeVisible(targetView, horizontalSnapPreference)
        val dy = calculateDyToMakeVisible(targetView, verticalSnapPreference)
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
        val time = calculateTimeForDeceleration(distance)
        if (time > 0) {
            action.update(-dx, -dy, duration.coerceAtLeast(time), interceptor)
        }
    }

    /**
     * 좀 떨어져 있는 곳에서 호출 하는 경우 아래 함수 호출
     */
    override fun updateActionForInterimTarget(action: Action?) {
        if (action == null) return

        val scrollVector = computeScrollVectorForPosition(targetPosition)
        if (scrollVector == null || (scrollVector.x == 0F && scrollVector.y == 0F)) {
            val target = targetPosition
            action.jumpTo(target)
            stop()
            return
        }
        normalize(scrollVector)
        mTargetVector = scrollVector
        mInterimTargetDx = (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.x).toInt()
        mInterimTargetDy = (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y).toInt()
        val time =
            calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX) * TARGET_SEEK_EXTRA_SCROLL_RATIO
        action.update(
            (mInterimTargetDx * TARGET_SEEK_EXTRA_SCROLL_RATIO).toInt(),
            (mInterimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO).toInt(),
            time.toInt().coerceAtLeast(duration),
            interceptor
        )
    }
}
