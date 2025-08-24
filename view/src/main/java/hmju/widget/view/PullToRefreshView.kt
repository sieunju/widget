package hmju.widget.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Description :
 *
 * Created by juhongmin on 2025. 8. 24.
 */
class PullToRefreshView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    interface OnRefreshListener {
        fun onRefresh()
        fun onPullProgress(progress: Float) // 0.0f ~ 1.0f
    }

    private var onRefreshListener: OnRefreshListener? = null
    private var scrollableView: View? = null
    private var refreshHeaderView: View? = null

    // 설정 가능한 값들
    private var refreshTriggerDistance = 150.dp
    private var maxPullDistance = 300.dp
    private var refreshHeaderHeight = 80.dp

    // 상태 관리
    private var isRefreshing = false
    private var isPulling = false
    private var currentPullDistance = 0f

    // 터치 관련
    private var startY = 0f
    private var lastY = 0f
    private var isDragging = false

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    init {
        // 하위 뷰들을 찾아서 설정
        post { setupViews() }
    }

    private fun setupViews() {
        // ScrollView 또는 RecyclerView 찾기
        scrollableView = findScrollableView(this)

        // Refresh Header View 찾기 (특정 태그나 ID로)
        refreshHeaderView = findRefreshHeaderView(this)

        setupRefreshHeader()
    }

    private fun findScrollableView(parent: ViewGroup): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            when (child) {
                is ScrollView, is NestedScrollView, is RecyclerView -> return child
                is ViewGroup -> {
                    val found = findScrollableView(child)
                    if (found != null) return found
                }
            }
        }
        return null
    }

    private fun findRefreshHeaderView(parent: ViewGroup): View? {
        // "refresh_header" 태그를 가진 뷰 찾기
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.tag == "refresh_header") {
                return child
            }
            if (child is ViewGroup) {
                val found = findRefreshHeaderView(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun setupRefreshHeader() {
        refreshHeaderView?.let { header ->
            // 처음에는 숨김
            header.translationY = -refreshHeaderHeight.toFloat()
            header.visibility = View.VISIBLE
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                lastY = ev.y
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - startY

                // 아래로 당기는 제스처이고, 스크롤뷰가 맨 위에 있을 때
                if (deltaY > 0 && isScrollViewAtTop() && !isRefreshing) {
                    isDragging = true
                    return true // 터치 이벤트를 가로챔
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - startY

                if (isDragging && deltaY > 0) {
                    // 당기는 거리 계산 (감쇠 효과 적용)
                    currentPullDistance = calculatePullDistance(deltaY)

                    // 헤더 뷰 위치 업데이트
                    updateRefreshHeader(currentPullDistance)

                    // 진행률 콜백
                    val progress = (currentPullDistance / refreshTriggerDistance).coerceAtMost(1f)
                    onRefreshListener?.onPullProgress(progress)

                    isPulling = true
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false

                    if (currentPullDistance >= refreshTriggerDistance && !isRefreshing) {
                        // 새로고침 트리거
                        triggerRefresh()
                    } else {
                        // 원래 위치로 복귀
                        resetPull()
                    }
                    isPulling = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isScrollViewAtTop(): Boolean {
        return when (val view = scrollableView) {
            is ScrollView -> view.scrollY == 0
            is NestedScrollView -> view.scrollY == 0
            is RecyclerView -> {
                val layoutManager = view.layoutManager
                when (layoutManager) {
                    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition() == 0
                    is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition() == 0
                    else -> !view.canScrollVertically(-1) // 위로 스크롤 불가능한 상태
                }
            }

            else -> true
        }
    }

    private fun calculatePullDistance(rawDelta: Float): Float {
        // 감쇠 효과: 당길수록 저항이 증가
        val resistance = when {
            rawDelta < refreshTriggerDistance -> 1f
            rawDelta < maxPullDistance -> 0.5f
            else -> 0.2f
        }

        return (rawDelta * resistance).coerceAtMost(maxPullDistance.toFloat())
    }

    private fun updateRefreshHeader(pullDistance: Float) {
        refreshHeaderView?.let { header ->
            // 헤더를 점진적으로 나타냄
            val translationY = -refreshHeaderHeight + pullDistance
            header.translationY = translationY.coerceAtMost(0f)

            // 추가 효과: 회전, 스케일 등
            val progress = (pullDistance / refreshTriggerDistance).coerceAtMost(1f)
            header.alpha = progress
            header.scaleX = 0.8f + (0.2f * progress)
            header.scaleY = 0.8f + (0.2f * progress)
        }
    }

    private fun triggerRefresh() {
        isRefreshing = true

        // 헤더를 완전히 보이게 애니메이션
        refreshHeaderView?.animate()
            ?.translationY(0f)
            ?.alpha(1f)
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.setDuration(200)
            ?.setInterpolator(DecelerateInterpolator())
            ?.start()

        // 새로고침 콜백 호출
        onRefreshListener?.onRefresh()
    }

    private fun resetPull() {
        // 원래 상태로 복귀 애니메이션
        refreshHeaderView?.animate()
            ?.translationY(-refreshHeaderHeight.toFloat())
            ?.alpha(0f)
            ?.scaleX(0.8f)
            ?.scaleY(0.8f)
            ?.setDuration(200)
            ?.setInterpolator(DecelerateInterpolator())
            ?.start()

        currentPullDistance = 0f
        onRefreshListener?.onPullProgress(0f)
    }

    // Public methods
    fun setOnRefreshListener(listener: OnRefreshListener?) {
        onRefreshListener = listener
    }

    fun setRefreshing(refreshing: Boolean) {
        if (isRefreshing == refreshing) return

        isRefreshing = refreshing

        if (refreshing) {
            // 새로고침 시작
            refreshHeaderView?.let { header ->
                header.translationY = 0f
                header.alpha = 1f
                header.scaleX = 1f
                header.scaleY = 1f
            }
        } else {
            // 새로고침 완료
            resetPull()
        }
    }

    fun setRefreshTriggerDistance(distance: Int): PullToRefreshView {
        refreshTriggerDistance = distance.dp
        return this
    }

    fun setMaxPullDistance(distance: Int): PullToRefreshView {
        maxPullDistance = distance.dp
        return this
    }

    fun setRefreshHeaderHeight(height: Int): PullToRefreshView {
        refreshHeaderHeight = height.dp
        setupRefreshHeader()
        return this
    }
}
