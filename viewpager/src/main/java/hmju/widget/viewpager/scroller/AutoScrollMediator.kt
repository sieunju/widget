package hmju.widget.viewpager.scroller

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager2.widget.ViewPager2

/**
 * Description : ViewPager2 기반의 자동 스크롤
 *
 * Created by juhongmin on 2022/01/19
 */
@SuppressLint("ClickableViewAccessibility")
@Suppress("unused", "MemberVisibilityCanBePrivate")
class AutoScrollMediator(
    private val viewPager: ViewPager2,
    private val delayTime: Long = 3000L
) {

    private var isStopByTouch = false
    // private var disposable: Disposable? = null

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

    fun startAutoScroll() {
//        disposable?.dispose()
//        disposable = null
//        disposable =
//            Observable.interval(
//                delayTime, TimeUnit.MILLISECONDS,
//                AndroidSchedulers.mainThread()
//            ).doOnNext {
//                // 페이지가 하나인경우 Interval 중지
//                if (viewPager.itemCount <= viewPager.currentItem.plus(1)) {
//                    throw IllegalArgumentException("Not AutoScroll")
//                }
//            }.subscribe({
//                smoothScrollItem(viewPager.currentItem.plus(1))
//            }, {
//                Timber.d("ERROR $it ${viewPager.tag}")
//            })
    }

    /**
     * Stop Auto Scroll
     */
    fun stopAutoScroll() {
//        disposable?.dispose()
//        disposable = null
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
                override fun onAnimationStart(animation: Animator?) {
                    viewPager.beginFakeDrag()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    viewPager.endFakeDrag()
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationRepeat(animation: Animator?) {}
            })
            this.interpolator = interpolator
            this.duration = duration
            start()
        }
    }
}
