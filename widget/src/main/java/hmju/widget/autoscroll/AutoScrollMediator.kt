package hmju.widget.autoscroll

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.extensions.getFragmentActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Description : ViewPager2 기반의 자동 스크롤
 *
 * Created by juhongmin on 2022/01/19
 */
@SuppressLint("ClickableViewAccessibility")
class AutoScrollMediator(
    private val viewPager: ViewPager2,
    private val delayTime: Long = 3000L
) : LifecycleObserver {
    private var isStopByTouch = false
    private var disposable: Disposable? = null
    private var activity: FragmentActivity? = null

    init {
        activity = viewPager.getFragmentActivity()?.apply {
            lifecycle.addObserver(this@AutoScrollMediator)
        }

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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        startAutoScroll()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        stopAutoScroll()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        activity?.lifecycle?.removeObserver(this)
    }

    fun startAutoScroll() {
        disposable?.dispose()
        disposable = null
        disposable =
            Observable.interval(delayTime, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext {
                    smoothScrollItem(viewPager.currentItem.plus(1))
                }.subscribe()
    }

    /**
     * Stop Auto Scroll
     */
    fun stopAutoScroll() {
        disposable?.dispose()
        disposable = null
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
        interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
        pageWidth: Int = viewPager.width
    ) {
        val pxToDrag = pageWidth * (pos - viewPager.currentItem)
        val animator = ValueAnimator.ofInt(0, pxToDrag)
        var prevValue = 0
        animator.addUpdateListener { valueAnimator ->
            val currentValue = valueAnimator.animatedValue as Int
            val currentPxToDrag = (currentValue - prevValue).toFloat()
            viewPager.fakeDragBy(-currentPxToDrag)
            prevValue = currentValue
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                viewPager.beginFakeDrag()
            }

            override fun onAnimationEnd(animation: Animator?) {
                viewPager.endFakeDrag()
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animator.interpolator = interpolator
        animator.duration = duration
        animator.start()
    }

    /**
     * AddObserver
     * @param activity 자동으로 추가해주는 Observer 에서 Activity 찾지 못하는 경우
     * 수동으로 처리하는 함수 like.. Hilt 를 사용하는 경우 기본 getFragmentActivity 확장함수에서는
     * 찾지 못함.
     */
    fun addObserver(activity: FragmentActivity) {
        activity.lifecycle.addObserver(this)
    }
}