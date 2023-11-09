package hmju.widget.viewpager.scroller

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager2.widget.ViewPager2

/**
 * Description : ViewPager2 기반의 자동 스크롤
 * 최대한 라이브러리를 사용하지 않기 위함으로 Handler 로 사용했지만, 되도록이면 아래 코드를 참고하여
 * Rx or Coroutines 로 처리하시기 바랍니다.
 * Created by juhongmin on 2022/01/19
 */
@SuppressLint("ClickableViewAccessibility")
@Suppress("unused", "MemberVisibilityCanBePrivate")
class AutoScrollMediator(
    private val viewPager: ViewPager2,
    private val delayTime: Long = 3000L
) {

    private val HANDLER_WHAT = 100
    private val handler: AutoScrollHandler by lazy { AutoScrollHandler() }
    private var isStopByTouch = false

    init {
        // ViewPager2 Touch Listener
        viewPager.getChildAt(0).setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_DOWN -> {
                    if (!isStopByTouch) {
                        isStopByTouch = true
                        stopAutoScroll()
                    }
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    // 한번만 실행 되도록
                    if (isStopByTouch) {
                        isStopByTouch = false
                        startAutoScroll()
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * Start Auto Scroll!
     */
    fun startAutoScroll() {
        handler.resetDispose()
        sendNextEvent()
    }

    /**
     * Stop Auto Scroll
     */
    fun stopAutoScroll() {
        handler.dispose()
        handler.removeMessages(HANDLER_WHAT)
    }

    /**
     * ViewPager2 Smooth Scroll Item
     * @param pos Next Item Pos
     * @param duration Delay Time
     * @param interpolator Animation Interpolator 점점 천천히
     * @param pageWidth ViewPager2 Width
     */
    private fun smoothScrollItem(
        pos: Int,
        duration: Long = 500L,
        interpolator: TimeInterpolator = FastOutSlowInInterpolator(),
        pageWidth: Int = viewPager.width
    ) {
        val pxToDrag = pageWidth * (pos - viewPager.currentItem)
        var prevValue = 0
        ValueAnimator.ofInt(0, pxToDrag).apply {
            addUpdateListener {
                val currentValue = it.animatedValue as Int
                val currentPxToDrag = (currentValue - prevValue).toFloat()
                viewPager.fakeDragBy(-currentPxToDrag)
                prevValue = currentValue
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    viewPager.beginFakeDrag()
                }

                override fun onAnimationEnd(animation: Animator) {
                    viewPager.endFakeDrag()
                    sendNextEvent()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            this.interpolator = interpolator
            this.duration = duration
            start()
        }
    }

    private fun sendNextEvent() {
        handler.sendEmptyMessageDelayed(HANDLER_WHAT, delayTime)
    }

    @SuppressLint("HandlerLeak")
    internal inner class AutoScrollHandler : Handler(Looper.getMainLooper()) {

        private var isDispose: Boolean = false

        fun dispose() {
            isDispose = true
        }

        fun resetDispose() {
            isDispose = false
        }

        override fun handleMessage(msg: Message) {
            if (isDispose) {
                return
            }

            runCatching {
                val itemCount = viewPager.adapter?.itemCount ?: 0
                if (itemCount <= viewPager.currentItem.plus(1)) {
                    return
                }

                removeMessages(HANDLER_WHAT)
                smoothScrollItem(viewPager.currentItem.plus(1))
            }
        }
    }
}
