package hmju.widget.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Description : PullToRefreshView..
 *
 * Created by juhongmin on 2025. 8. 24.
 */
@Suppress("unused")
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

    enum class ScrollType {
        NONE, TRANSLATION
    }

    interface Listener {
        /**
         * Call Refresh
         */
        fun onRefresh()

        /**
         * onPullProgress 0.0f ~ 1.0f
         */
        fun onPullProgress(progress: Float) // 0.0f ~ 1.0f
    }

    /**
     * Scroll 할때 상태에 대한 데이터 모델
     */
    private class State {
        var hasRefresh: Boolean = false
        var hasPulling: Boolean = false
        var pullDistance: Float = 0.0f
        var startY: Float = 0f
        var hasDragging: Boolean = false
    }

    // [Variable]
    private var listener: Listener? = null
    private var scrollType: ScrollType = ScrollType.NONE
    private var vScroll: View? = null
    private var vRefresh: View? = null
    private var refreshHeaderHeight = 0
    private var triggerDistance = 0
    private var maxPullDistance = 0
    private var scrollResistance = 0.3f
    // [Variable]

    private val state: State by lazy { State() }

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    init {
        setRefreshHeaderHeight(100)
        setTriggerDistance(100)
        setMaxPullDistance(100)

    }

    fun setRefresh(hasRefresh: Boolean) {
        if (state.hasRefresh == hasRefresh) return
        state.hasRefresh = hasRefresh
        if (hasRefresh) {
            handleStartRefreshWithAni()
        } else {
            handleResetViewWithAni()
        }
    }

    fun setScrollType(type: ScrollType): PullToRefreshView {
        scrollType = type
        return this
    }

    fun setListener(listener: Listener): PullToRefreshView {
        this.listener = listener
        return this
    }

    /**
     * onRefresh 하기위한 Refresh Distance 값
     * @see maxPullDistance 보다 작아야 onRefresh 성립됩니다.
     */
    fun setTriggerDistance(distance: Int): PullToRefreshView {
        triggerDistance = distance.dp
        return this
    }

    /**
     * Refresh 가능한 Scroll Distance
     * @see vRefresh 높이보다 커야합니다.
     */
    fun setMaxPullDistance(distance: Int): PullToRefreshView {
        maxPullDistance = distance.dp
        return this
    }

    fun setRefreshHeaderHeight(height: Int): PullToRefreshView {
        refreshHeaderHeight = height.dp
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
        if (scrollType == ScrollType.TRANSLATION) {
            vRefresh?.also {
                it.translationY = -refreshHeaderHeight.toFloat()
                it.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        findScrollView(view)
        findRefreshView(view)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                state.startY = ev.y
                state.hasDragging = false
                state.hasPulling = false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - state.startY
                if (deltaY > 0 && isScrollViewAtTop() && !state.hasRefresh) {
                    state.hasDragging = true
                    state.hasPulling = true
                    state.pullDistance = calculatePullDistance(deltaY)
                    if (scrollType == ScrollType.TRANSLATION) {
                        vRefresh?.translationY =
                            0f.coerceAtMost(-refreshHeaderHeight + state.pullDistance)
                    }
                    vScroll?.translationY = Math.min(state.pullDistance, maxPullDistance.toFloat())
                    val progress = (state.pullDistance / triggerDistance).coerceAtMost(1f)
                    listener?.onPullProgress(progress)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                state.startY = ev.y
                if (state.hasDragging && state.hasPulling) {
                    state.hasDragging = false
                    state.hasPulling = false
                    if (state.pullDistance >= triggerDistance && !state.hasRefresh) {
                        handleStartRefreshWithAni()
                    } else {
                        handleResetViewWithAni()
                    }
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
                    else -> !view.canScrollVertically(-1)
                }
            }

            else -> true
        }
    }

    private fun calculatePullDistance(rawDelta: Float): Float {
        return (rawDelta * scrollResistance).coerceAtMost(maxPullDistance.toFloat())
    }

    private fun handleStartRefreshWithAni() {
        state.hasRefresh = true
        val animators = mutableListOf<Animator>()
        if (scrollType == ScrollType.TRANSLATION) {
            vRefresh?.let { v ->
                ObjectAnimator.ofPropertyValuesHolder(
                    v,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
                )
            }?.let { animators.add(it) }
        }

        vScroll?.let { v ->
            ObjectAnimator.ofPropertyValuesHolder(
                v,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, refreshHeaderHeight.toFloat())
            )
        }?.let { animators.add(it) }

        AnimatorSet().apply {
            playTogether(animators)
            interpolator = DecelerateInterpolator()
            duration = 200
            doOnEnd { listener?.onRefresh() }
            start()
        }
    }

    /**
     * 뷰 초기화 처리 함수 자연스러운 애니메이션으로 처리
     */
    private fun handleResetViewWithAni() {
        val animators = mutableListOf<Animator>()
        vRefresh?.let {
            if (scrollType == ScrollType.TRANSLATION) {
                ObjectAnimator.ofPropertyValuesHolder(
                    it,
                    PropertyValuesHolder.ofFloat(
                        View.TRANSLATION_Y,
                        -refreshHeaderHeight.toFloat()
                    ),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f)
                )
            } else {
                // ScrollType None은 사용자에게 비즈니스 로직을 처리합니다.
                null
            }
        }?.let { animators.add(it) }

        vScroll?.let {
            ObjectAnimator.ofPropertyValuesHolder(
                it,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f)
            )
        }?.let { animators.add(it) }

        if (animators.isNotEmpty()) {
            AnimatorSet().apply {
                playTogether(animators)
                interpolator = DecelerateInterpolator()
                duration = 200
                doOnEnd {
                    state.pullDistance = 0f
                    listener?.onPullProgress(0f)
                }
                start()
            }
        } else {
            // 애니메이션할 뷰가 없어도 상태는 리셋
            state.pullDistance = 0f
            listener?.onPullProgress(0f)
        }
    }
}