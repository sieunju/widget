package hmju.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.Math.abs
import java.lang.Math.atan2

/**
 * Description : RecyclerView
 * 수평으로 제스처 하는경우
 * 터치한 View 가 수평인경우 해당 뷰 포커싱 하도록 처리.
 *
 * Created by juhongmin on 7/19/21
 */
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
                    findChildViewUnder(event.x, event.y)?.let { underChildView ->
                        if (underChildView is ViewGroup) {
                            computeHorizontalView(underChildView, prevX, event.x, prevY, event.y)
                        }
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(event)
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
        var result = Math.toDegrees(kotlin.math.atan2(deltaY, deltaX).toDouble())
        result = kotlin.math.abs(result)
        // 수평 제스처 기준 0~45, 135~180
        return if (0.0 < result && result < leftHorizontalDegree) {
            true
        } else rightHorizontalDegree < result && result < 180.0
    }

    private fun computeHorizontalView(
        parent: ViewGroup,
        prevX: Float,
        currX: Float,
        prevY: Float,
        currY: Float
    ) {
        for (idx in 0 until parent.childCount) {
            when (val view = parent[idx]) {
                is ViewPager2 -> {
                    if (view.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                        // 수평 제스처
                        if (isHorizontalSwipe(prevX, currX, prevY, currY)) {
                            val diffX = currX - prevX
                            // 좌 -> 우
                            if (diffX > 0) {
                                if (view.currentItem == 0) {
                                    requestDisallowInterceptTouchEvent(false)
                                } else {
                                    requestDisallowInterceptTouchEvent(true)
                                }
                            } else {
                                // 우 -> 좌
                                val itemCount = view.adapter?.itemCount ?: 0
                                if (view.currentItem == itemCount.minus(1)) {
                                    requestDisallowInterceptTouchEvent(false)
                                } else {
                                    requestDisallowInterceptTouchEvent(true)
                                }
                            }
                            return
                        } else {
                            requestDisallowInterceptTouchEvent(false)
                            return
                        }
                    }
                }
                is RecyclerView -> {
                    if (view.layoutManager is LinearLayoutManager &&
                        (view.layoutManager as LinearLayoutManager).orientation == HORIZONTAL
                    ) {
                        // 수평으로 하는 제스처인경우
                        if (isHorizontalSwipe(prevX, currX, prevY, currY)) {
                            val layoutManager = (view.layoutManager as LinearLayoutManager)
                            val firstPosition =
                                layoutManager.findFirstCompletelyVisibleItemPosition()
                            val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                            val lastIndex = layoutManager.itemCount.minus(1)
                            val diffX = currX - prevX
                            // 좌 -> 우
                            if (diffX > 0) {
                                if (firstPosition == 0) {
                                    requestDisallowInterceptTouchEvent(false)
                                } else {
                                    requestDisallowInterceptTouchEvent(true)
                                }
                            } else {
                                // 우 -> 좌
                                if (lastPosition == lastIndex) {
                                    requestDisallowInterceptTouchEvent(false)
                                } else {
                                    requestDisallowInterceptTouchEvent(true)
                                }
                            }
                            return
                        } else {
                            requestDisallowInterceptTouchEvent(false)
                            return
                        }
                    }
                }
                else -> {}
            }
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // true -> Child View 포커싱 주기 false -> Parent 포커싱 주기
//        if (disallowIntercept) {
//            Timber.d("ChildView 에 포커싱을 줍니다.")
//        } else {
//            Timber.d("Parent 에 포커싱을 줍니다. ")
//        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}