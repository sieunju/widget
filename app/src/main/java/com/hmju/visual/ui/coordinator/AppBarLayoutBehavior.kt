package com.hmju.visual.ui.coordinator

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import java.lang.reflect.Field

/**
 * Description : AppBarLayout 스크롤할때 가끔 스크롤이 떠는 이슈에 대한 대응 코드
 * 1) Fast sliding appbarLayout will rebound
 * 2) Fast sliding appbarLayout to fold state, immediately sliding down, there will be the problem of jitter.
 * 3) Slide appbarLayout, unable to stop sliding by pressing it with your finger
 * Created by juhongmin on 3/3/24
 */
class AppBarLayoutBehavior(
    context: Context?,
    attrs: AttributeSet?
) : AppBarLayout.Behavior(context, attrs) {

    private var isFlinging = false
    private var shouldBlockNestedScroll = false

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        shouldBlockNestedScroll = isFlinging
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> stopAppbarLayoutFling(child)
            else -> {
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    /**
     * Reflect to get private flingRunnable attributes, considering the problem of variable name modification after support 28
     *
     * @return Field
     * @throws NoSuchFieldException
     */
    @get:Throws(NoSuchFieldException::class)
    private val flingRunnableField: Field?
        get() {
            val superclass: Class<*>? = this.javaClass.superclass
            return try {
                // Support design 27 and the following version
                var headerBehaviorType: Class<*>? = null
                if (superclass != null) {
                    headerBehaviorType = superclass.superclass
                }
                headerBehaviorType?.getDeclaredField("mFlingRunnable")
            } catch (e: NoSuchFieldException) {
                // Possibly 28 or more versions
                val headerBehaviorType = superclass?.superclass?.superclass
                headerBehaviorType?.getDeclaredField("flingRunnable")
            }
        } // Possibly 28 or more versions// Support design 27 and the following version

    /**
     * Reflect to get private scroller attributes, considering the problem of variable name modification after support 28
     *
     * @return Field
     * @throws NoSuchFieldException
     */
    @get:Throws(NoSuchFieldException::class)
    private val scrollerField: Field?
        get() {
            val superclass: Class<*>? = this.javaClass.superclass
            return try {
                // Support design 27 and the following version
                var headerBehaviorType: Class<*>? = null
                if (superclass != null) {
                    headerBehaviorType = superclass.superclass
                }
                headerBehaviorType?.getDeclaredField("mScroller")
            } catch (e: NoSuchFieldException) {
                // Possibly 28 or more versions
                val headerBehaviorType = superclass?.superclass?.superclass
                headerBehaviorType?.getDeclaredField("scroller")
            }
        }

    /**
     * Stop appbarLayout's fling event
     *
     * @param appBarLayout
     */
    private fun stopAppbarLayoutFling(appBarLayout: AppBarLayout) {
        try {
            val flingRunnableField = flingRunnableField
            val scrollerField = scrollerField
            if (flingRunnableField != null) {
                flingRunnableField.isAccessible = true
            }
            if (scrollerField != null) {
                scrollerField.isAccessible = true
            }
            var flingRunnable: Runnable? = null
            if (flingRunnableField != null) {
                flingRunnable = flingRunnableField[this] as? Runnable
            }
            val overScroller = scrollerField?.get(this) as? OverScroller
            if (flingRunnable != null) {
                // Timber.d("Flying Runnable")
                appBarLayout.removeCallbacks(flingRunnable)
                flingRunnableField?.set(this, null)
            }
            if (overScroller != null && !overScroller.isFinished) {
                overScroller.abortAnimation()
            }
        } catch (e: NoSuchFieldException) {
            // nothing
        } catch (e: IllegalAccessException) {
            // nothing
        }
    }

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        stopAppbarLayoutFling(child)
        return super.onStartNestedScroll(
            parent,
            child,
            directTargetChild,
            target,
            nestedScrollAxes,
            type
        )
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // Timber.d("onNestedPreScroll: ${child.totalScrollRange}, dx: $dx, dy: $dy, type: $type")
        // When type returns to 1, it indicates that the current target is in a non-touch sliding.
        // The bug is caused by the sliding of the NestedScrolling Child2 interface in Coordinator Layout when the AppBar is sliding
        // The subclass has not ended its own fling
        // So here we listen for non-touch sliding of subclasses, and then block the sliding event to AppBarLayout
        if (type == TYPE_FLING) {
            isFlinging = true
        }
        if (!shouldBlockNestedScroll) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (!shouldBlockNestedScroll) {
            @Suppress("DEPRECATION")
            super.onNestedScroll(
                coordinatorLayout,
                child,
                target,
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                type
            )
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        abl: AppBarLayout,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
        isFlinging = false
        shouldBlockNestedScroll = false
    }

    companion object {
        private const val TYPE_FLING = 1
    }
}
