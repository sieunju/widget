package hmju.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.core.view.forEach
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

    @FloatRange(from = 0.0, to = 45.0)
    var startHorizontalDegree: Double = 0.0

    @FloatRange(from = 45.0, to = 90.0)
    var endHorizontalDegree: Double = 60.0 // Horizontal Degree

    @FloatRange(from = 0.0, to = 25.0)
    var startVerticalDegree: Double = 0.0

    @FloatRange(from = 25.0, to = 45.0)
    var endVerticalDegree: Double = 45.0 // Vertical Degree

    var diffLocationGap: Float = 30F // 해당 차이값에 따라서 이전 좌표를 저장유무를 판단 한다.

    private var prevX = -1F
    private var prevY = -1F
    private var isTouchRunning = false // 현재 터치중인지 true -> 터치중, false -> 터치 종료

    init {
        isFocusable = false
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let { evt ->
            // 초기값 세팅
            if (prevX == -1F) evt.x
            if (prevY == -1F) evt.y

            findChildViewUnder(evt.x, evt.y)?.let { underChildView ->
                val diffX = abs(evt.x.toDouble() - prevX.toDouble())
                val diffY = abs(evt.y.toDouble() - prevY.toDouble())
                if (underChildView is ViewGroup) {
                    if (findHorizontalView(underChildView)) {
                        if (diffX.toInt() == 0 || diffY.toInt() == 0) {
                            // 변화가 없는 경우 스킵.
                            underChildView.forEach { view->
                                if(view is RecyclerView) {
                                    val range = view.computeHorizontalScrollRange() // Scroll All Range
                                    val offset = view.computeHorizontalScrollOffset() // Scroll Offset
                                    val extent = view.computeHorizontalScrollExtent() // Device Width
                                    // 좌, 우 스크롤 불가능일때 부모한테 터치 권한 줌.
                                    if(offset == 0 || range == (offset + extent)) {
                                        super.requestDisallowInterceptTouchEvent(false)
                                    }
                                    return@forEach
                                }
                            }
                        } else {
                            if (isHorizontalSwipe(diffX, diffY)) {
                                super.requestDisallowInterceptTouchEvent(true)
                            } else {
                                super.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                }
            }

            prevX = evt.x
            prevY = evt.y
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 제스처가 수평인지 확인하는 함수.
     * @param diffX 이전 X 좌표와 현재 X 좌표 차이값
     * @param diffY 이전 Y 좌표와 현재 Y 좌표 차이값
     * @return true -> 수평 제스처, false -> 수평 제스처 범위 아님.
     */
    private fun isHorizontalSwipe(diffX: Double, diffY: Double): Boolean {
        val diffRotate = Math.toDegrees(atan2(diffY, diffX))
        if (startHorizontalDegree < diffRotate && diffRotate < endHorizontalDegree) {
            return true
        }
        return false
    }

    private fun isVerticalSwipe(diffX: Double, diffY: Double): Boolean {
        val diffRotate = Math.toDegrees(atan2(diffY, diffX))
        if (startVerticalDegree < diffRotate && diffRotate < endVerticalDegree) {
            return true
        }
        return false
    }

    /**
     * 수평으로된 레이아웃 찾기
     * @param parent ViewGroup
     * @return true -> 해당 ViewGroup 안에 수평으로된 레이아웃이 있습니다.
     * false -> 없습니다.
     */
    private fun findHorizontalView(parent: ViewGroup): Boolean {
        parent.forEach {
            when (it) {
                is ViewPager2 -> {
                    if (it.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                        return true
                    }
                }
                is RecyclerView -> {
                    if (it.layoutManager is LinearLayoutManager &&
                        (it.layoutManager as LinearLayoutManager).orientation == HORIZONTAL
                    ) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // true -> Child View 포커싱 주기 false -> Parent 포커싱 주기
//		JLogger.d("onRequestDisallowInterceptTouchEvent $disallowIntercept")
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}