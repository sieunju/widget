package hmju.widget.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Description : PullToRefreshView..
 *
 * Created by juhongmin on 2025. 8. 24.
 */
class PullToRefreshView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "PullToRefreshView"
        private const val DEBUG = true
        private fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    interface Listener {
        fun onRefresh()

        /**
         * onPullProgress 0.0f ~ 1.0f
         */
        fun onPullProgress(progress: Float) // 0.0f ~ 1.0f
    }

    private var listener: Listener? = null
    private var vScroll: View? = null
    private var vRefresh: View? = null
    private var triggerDistance = 150.dp
    private var maxPullDistance = 300.dp
    private var refreshHeaderHeight = 80.dp // RefreshHeader 고정이어야 한다.

    private var isRefreshing = false
    private var isPulling = false
    private var currentPullDistance = 0f

    private var startY = 0f
    private var isDragging = false

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setRefreshing(refreshing: Boolean) {
        if (isRefreshing == refreshing) return
        isRefreshing = refreshing
        if (refreshing) {
            handleStartRefreshWithAni()
        } else {
            handleResetViewWithAni()
        }
    }

    private fun setRefreshTriggerDistance(distance: Int): PullToRefreshView {
        triggerDistance = distance.dp
        return this
    }

    /**
     *
     */
    fun setMaxPullDistance(distance: Int): PullToRefreshView {
        maxPullDistance = distance.dp
        return this
    }

    fun setRefreshHeaderHeight(height: Int): PullToRefreshView {
        refreshHeaderHeight = height.dp
        setRefreshTriggerDistance((height * 1.5).toInt())
        setupRefreshHeader()
        return this
    }

    private fun findScrollView(view: View?) {
        if (vScroll != null) return
        if (view == null || view !is ViewGroup) return
        if (view is ScrollView) {
            vScroll = view
        } else if (view is NestedScrollView) {
            vScroll = view
        } else if (view is RecyclerView) {
            vScroll = view
        }
    }

    private fun findRefreshView(view: View?) {
        if (vRefresh != null) return
        if (view == null) return
        if (view.tag == "refresh_header") {
            vRefresh = view
            setupRefreshHeader()
        }
    }

    private fun setupRefreshHeader() {
        vRefresh?.also { header ->
            header.translationY = -refreshHeaderHeight.toFloat()
            header.visibility = View.VISIBLE
        }
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        LogD("onViewAdded ${view}")
        findScrollView(view)
        findRefreshView(view)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val actionText = when (ev.action) {
            MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
            MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
            MotionEvent.ACTION_UP -> "ACTION_UP"
            MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
            else -> "Unknown"
        }
        // LogD("dispatchTouchEvent $actionText ${ev.y.toInt()} ${isDragging}")
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                isDragging = false
                isPulling = false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - startY
                // Scroll Down
                if (deltaY > 0 && isScrollViewAtTop() && !isRefreshing) {
                    isDragging = true
                    isPulling = true

                    currentPullDistance = calculatePullDistance(deltaY)
                    updateUi(currentPullDistance)
                    val progress = (currentPullDistance / triggerDistance).coerceAtMost(1f)
                    listener?.onPullProgress(progress)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                startY = ev.y
                if (isDragging && isPulling) {
                    isDragging = false
                    isPulling = false

                    if (currentPullDistance >= triggerDistance && !isRefreshing) {
                        handleStartRefreshWithAni()
                    } else {
                        handleResetViewWithAni()
                    }

                    // 터치 이벤트를 소비
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isScrollViewAtTop(): Boolean {
        return when (val view = vScroll) {
            is ScrollView -> view.scrollY == 0
            is NestedScrollView -> view.scrollY == 0
            is RecyclerView -> {
                when (val layoutManager = view.layoutManager) {
                    is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition() == 0
                    else -> !view.canScrollVertically(-1) // 위로 스크롤 불가능한 상태
                }
            }

            else -> true
        }
    }

    /**
     * 당길때 스크롤 저항
     */
    private fun calculatePullDistance(rawDelta: Float): Float {
        val resistance = 0.4f
        return (rawDelta * resistance).coerceAtMost(maxPullDistance.toFloat())
    }

    private fun updateUi(pullDistance: Float) {
        // Math.min
        vRefresh?.translationY = 0f.coerceAtMost(-refreshHeaderHeight + pullDistance)
        // 최대 Refresh
        vScroll?.translationY = pullDistance.coerceAtMost(refreshHeaderHeight * 1.5f)
    }

    private fun handleStartRefreshWithAni() {
        isRefreshing = true
        vRefresh?.also {
            it.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        vScroll?.also {
            it.animate()
                .translationY(refreshHeaderHeight.toFloat())
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        listener?.onRefresh()
    }

    /**
     * 뷰 초기화 처리 함수 자연스러운 애니메이션으로 처리
     */
    private fun handleResetViewWithAni() {
        vRefresh?.also {
            it.animate()
                .translationY(-refreshHeaderHeight.toFloat())
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        vScroll?.also {
            it.animate()
                .translationY(0f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        currentPullDistance = 0f
        listener?.onPullProgress(0f)
    }
}