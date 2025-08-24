package hmju.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Description : Wallet Card Stack
 *
 * Created by juhongmin on 2025. 7. 25.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class WalletStackView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : ConstraintLayout(context, attrs, defStyleAttr) {

    open class SimpleListener<T> : Listener<T> {
        override fun initView(item: T, parent: ViewGroup): View {
            throw IllegalStateException("Required Method")
        }

        override fun onItemClick(item: T) {

        }

        override fun onStartAniCompleted() {

        }
    }

    interface Listener<T> {
        fun initView(item: T, parent: ViewGroup): View

        fun onItemClick(item: T)

        fun onStartAniCompleted()
    }

    internal open class WalletData<T>(
        var index: Int,
        val item: T
    )

    internal class ViewWrapperData<T>(
        val view: View,
        val data: WalletData<T>
    )

    private var listener: Listener<T>? = null
    private var spanStackHeight = 30.dp.toFloat()
    private var scaleIncrement = 0.1f
    private var alphaIncrement = 0.1f
    private var startTranslationY = 0f
    private var threshold = 200
    private val originList = mutableListOf<WalletData<T>>()
    private val virtualList: MutableList<WalletData<T>> = mutableListOf()
    private val viewList: MutableList<ViewWrapperData<T>> = mutableListOf()
    private var currentIndex = 0
    private var stackCount = 3
    private var centerX = 0
    private var isAni = false

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    init {
        clipToPadding = false
        clipChildren = false
        setStackCount(3)
        post { centerX = width / 2 }
    }

    fun setListener(l: Listener<T>?) {
        listener = l
    }

    fun setStackCount(newValue: Int): WalletStackView<T> {
        stackCount = newValue
        val ratio = 1.0f / newValue
        alphaIncrement = ceil(ratio * 10f) / 10f
        return this
    }

    fun setThreshold(newValue: Int): WalletStackView<T> {
        threshold = newValue
        return this
    }

    fun setScaleIncrement(newValue: Float): WalletStackView<T> {
        scaleIncrement = newValue
        return this
    }

    fun setSpanStackHeight(newHeight: Float): WalletStackView<T> {
        spanStackHeight = newHeight
        return this
    }

    fun setStartTranslationY(newValue: Float): WalletStackView<T> {
        startTranslationY = newValue
        return this
    }

    fun setItems(list: List<T>): WalletStackView<T> {
        originList.clear()
        for (i in list.indices) {
            originList.add(WalletData(i, list[i]))
        }
        return this
    }

    fun startAni() {
        if (listener == null) return
        removeAllViews()
        if (viewList.isNotEmpty()) {
            viewList.clear()
        }
        // Stack 값 보정
        val newList = mutableListOf<WalletData<T>>()
        val originSize = originList.size
        val needSize = stackCount.plus(1)
        val adjustSize = if (originSize >= needSize) {
            needSize
        } else {
            ((needSize + originSize - 1) / originSize) * originSize
        }
        for (i in 0 until adjustSize) {
            val item = originList[i % originSize]
            newList.add(WalletData(i, item.item))
        }
        virtualList.clear()
        virtualList.addAll(newList)
        val aniCompletedCount = intArrayOf(0)
        for (i in 0 until stackCount + 1) {
            val data = virtualList[i]
            val view = listener!!.initView(data.item, this)
            view.setTouchDetector(data)
            val scale = 1.0f - (i * scaleIncrement)
            view.setScale(scale)
            view.translationY = startTranslationY
            viewList.add(ViewWrapperData(view, data))
            addView(view, 0)
            view.animate().alpha(getIndexAlpha(i))
                .translationY(-spanStackHeight * i)
                .setStartDelay(50L * i)
                .setDuration(500)
                .setInterpolator(EaseOutInterpolator())
                .withEndAction {
                    aniCompletedCount[0]++
                    if (aniCompletedCount[0] == stackCount) {
                        listener!!.onStartAniCompleted()
                    }
                }
                .start()
        }
        currentIndex = virtualList[0].index
    }

    private fun View.setScale(newScale: Float) {
        scaleX = newScale
        scaleY = newScale
    }

    private fun View.setTouchDetector(item: WalletData<T>) {
        val touchListener: OnTouchListener = object : OnTouchListener {
            var currentX: Float = 0f
            var currentY: Float = 0f
            var touchStartTime: Long = 0
            var isMoved: Boolean = false

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (item.index != currentIndex) return false
                if (isAni) return false
                val action = event.action
                if (action == MotionEvent.ACTION_DOWN) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    currentX = event.rawX
                    currentY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    isMoved = false
                } else if (action == MotionEvent.ACTION_MOVE) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    val diffX = event.rawX - currentX
                    val diffY = event.rawY - currentY

                    val distance = sqrt((diffX * diffX + diffY * diffY).toDouble()).toFloat()
                    if (distance > CLICK_THRESHOLD_DP.dp) {
                        isMoved = true
                    }

                    v.translationX = diffX

                    // Y값과 회전 추가
                    var progress =
                        (abs(diffX.toDouble()) / (centerX * 0.5f)).toFloat() // 스와이프 진행도 (0~1)
                    progress = min(progress.toDouble(), 1.0).toFloat() // 최대 1.0으로 제한
                    // Y값: 스와이프할수록 아래로 내려감 (최대 10dp)
                    val translationY = 10.dp * progress
                    v.translationY = translationY

                    // 회전: 스와이프 방향에 따라 회전 (최대 8도)
                    val rotation = (if (diffX > 0) 1 else -1) * 8 * progress
                    v.rotation = rotation
                    updateBackViewsAnimation(progress)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    parent.requestDisallowInterceptTouchEvent(false)

                    val touchDuration = System.currentTimeMillis() - touchStartTime
                    val diffX = event.rawX - currentX
                    val diffY = event.rawY - currentY
                    val distance = sqrt((diffX * diffX + diffY * diffY).toDouble()).toFloat()

                    // 클릭 조건: 이동 거리가 적고, 시간이 짧고, 실제로 이동하지 않았을 때
                    if (!isMoved && distance < CLICK_THRESHOLD_DP.dp && touchDuration < CLICK_DURATION_THRESHOLD) {
                        // 클릭 이벤트 발생
                        listener!!.onItemClick(item.item)
                        resetCards()
                        touchStartTime = 0
                        isMoved = false
                        return true
                    }

                    val x = v.translationX
                    if (isRemove(x, threshold.toFloat())) {
                        removeFirstCard(v, x > 0)
                        rearrangeCards()
                    } else {
                        resetCards()
                    }
                }
                return true
            }
        }
        setOnTouchListener(touchListener)
    }

    private fun updateBackViewsAnimation(progress: Float) {
        for (i in 1 until viewList.size) {
            val backView = viewList[i].view

            // 현재 인덱스에서의 기본값
            val fromScale = 1.0f - (i * scaleIncrement)
            val fromTransY = -spanStackHeight * i
            val fromAlpha = 1.0f - (i * alphaIncrement)

            // 한 단계 위로 올라갔을 때의 값 (i-1 위치)
            val toScale = 1.0f - ((i - 1) * scaleIncrement)
            val toTransY = -spanStackHeight * (i - 1)
            val toAlpha = 1.0f - ((i - 1) * alphaIncrement)

            // 보간 계산
            val interpolatedScale = lerp(fromScale, toScale, progress)
            val interpolatedTransY = lerp(fromTransY, toTransY, progress)
            val interpolatedAlpha = lerp(fromAlpha, toAlpha, progress)
            backView.setScale(interpolatedScale)
            backView.translationY = interpolatedTransY
            backView.alpha = interpolatedAlpha
        }
    }

    /**
     * 수치 보정 하는 함수
     */
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    private fun isRemove(viewX: Float, centerX: Float): Boolean {
        // 우 -> 좌 마이 너스
        // 좌 -> 우 플러스
        // 양수 좌 -> 우
        return if (viewX > 0) {
            // 좌에서 우로 스와이프
            centerX - viewX <= 0 // centerX - viewX가 0 이하면 제거
        } else {
            // 우에서 좌로 스와이프
            centerX + viewX <= 0 // centerX + viewX가 0 이하면 제거
        }
    }

    private fun getIndexAlpha(index: Int): Float {
        return 1.0f - (index * alphaIncrement)
    }

    private fun getIndexScale(index: Int): Float {
        return 1.0f - (index * scaleIncrement)
    }

    private fun getIndexTranslationY(index: Int): Float {
        return -spanStackHeight * index
    }

    private fun removeFirstCard(view: View, isRightSwipe: Boolean) {
        val targetTranslationX = (if (isRightSwipe) width else -width).toFloat()
        val targetTranslationY = 30.dp.toFloat()
        val rotation = ((if (isRightSwipe) 1 else -1) * 8).toFloat()
        view.animate()
            .setDuration(300)
            .translationX(targetTranslationX)
            .translationY(targetTranslationY)
            .rotation(rotation)
            .alpha(0.5f)
            .setInterpolator(EaseOutInterpolator())
            .withEndAction { removeView(view) }
            .start()
        viewList.removeAt(0)
    }

    private fun rearrangeCards() {
        val totalSize = virtualList.size
        val lastIndex = viewList[viewList.size - 1].data.index
        val findNextIndex = if (lastIndex == (totalSize - 1)) 0 else lastIndex + 1
        val newItem = virtualList[findNextIndex]
        val newView = listener!!.initView(newItem.item, this)
        newView.setTouchDetector(newItem)
        newView.alpha = 0f
        viewList.add(ViewWrapperData(newView, newItem))
        addView(newView, 0)
        resetCards()
        currentIndex = viewList[0].data.index
    }

    private fun resetCards() {
        isAni = true
        val aniCount = intArrayOf(0)
        val aniTotalSize = viewList.size
        for (i in viewList.indices) {
            val view = viewList[i].view
            view.animate()
                .setDuration(100)
                .alpha(getIndexAlpha(i))
                .translationX(0f)
                .translationY(getIndexTranslationY(i))
                .scaleX(getIndexScale(i))
                .scaleY(getIndexScale(i))
                .rotation(0f)
                .setInterpolator(EaseOutInterpolator())
                .withEndAction {
                    aniCount[0]++
                    isAni = aniCount[0] != aniTotalSize
                }
                .start()
        }
    }

    private class EaseOutInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return 1f - (1f - input).toDouble().pow(2.0).toFloat()
        }
    }

    companion object {
        const val CLICK_THRESHOLD_DP: Int = 10
        const val CLICK_DURATION_THRESHOLD: Long = 200
    }
}
