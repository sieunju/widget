package hmju.widget.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Description : 수평으로 제스처 하는 경우
 * RecyclerView 에 터치 초점 맞춰주는 RecyclerView
 * RecyclerView
 *  -> RecyclerView
 *  인 구조에서는 ChildRecyclerView 에 해당 RecyclerView 를 추가 할것
 * 부모레벨 처리하도록 만든게 아닙니다.
 * Created by juhongmin on 7/19/21
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class FocusingRecyclerView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(ctx, attrs, defStyleAttr) {

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

    init {
        isFocusable = false
        isMotionEventSplittingEnabled = false
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    prevX = 0F
                    prevY = 0F
                }
                MotionEvent.ACTION_DOWN -> {
                    prevX = event.x
                    prevY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    performFocusingScrollView(prevX, event.x, prevY, event.y)
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    /**
     * 제스처 & 현재 위치에 따라서 터치 훔치는 처리함수
     */
    private fun performFocusingScrollView(prevX: Float, currX: Float, prevY: Float, currY: Float) {
        runCatching {
            if (layoutManager is LinearLayoutManager &&
                (layoutManager as LinearLayoutManager).orientation == HORIZONTAL
            ) {
                // 수평으로 하는 제스처인경우
                if (isHorizontalSwipe(prevX, currX, prevY, currY)) {
                    val tmpLayoutManager = (layoutManager as LinearLayoutManager)
                    val firstPosition =
                        tmpLayoutManager.findFirstCompletelyVisibleItemPosition()
                    val lastPosition =
                        tmpLayoutManager.findLastCompletelyVisibleItemPosition()
                    val lastIndex = tmpLayoutManager.itemCount.minus(1)
                    val diffX = currX - prevX

                    // 좌 -> 우
                    if (diffX > 0) {
                        if (firstPosition == 0) {
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    } else {
                        // 우 -> 좌
                        if (lastPosition == lastIndex) {
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
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

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // true -> Child View 포커싱 주기 false -> Parent 포커싱 주기
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}
