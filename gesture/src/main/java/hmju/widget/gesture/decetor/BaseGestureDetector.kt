package hmju.widget.gesture.decetor

import android.annotation.SuppressLint
import android.view.MotionEvent

/**
 * Description : BaseGestureDetector Class
 *
 * Created by juhongmin on 11/21/21
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BaseGestureDetector {

    protected var gestureInProgress = false

    protected var prevEvent: MotionEvent? = null
    private var currentEvent: MotionEvent? = null

    protected var currentPressure = 0f
    protected var prevPressure = 0f
    protected var timeDelta: Long = 0
    protected val PRESSURE_THRESHOLD = 0.67F

    /**
     * All gesture detectors need to be called through this method to be able to
     * detect gestures. This method delegates work to handler methods
     * (handleStartProgressEvent, handleInProgressEvent) implemented in
     * extending classes.
     *
     * @param event
     * @return
     */
    open fun onTouchEvent(event: MotionEvent): Boolean {
        val actionCode = event.action and MotionEvent.ACTION_MASK
        if (!gestureInProgress) {
            handleStartProgressEvent(actionCode, event)
        } else {
            handleInProgressEvent(actionCode, event)
        }
        return true
    }

    /**
     * Called when the current event occurred when NO gesture is in progress
     * yet. The handling in this implementation may set the gesture in progress
     * (via mGestureInProgress) or out of progress
     * @param actionCode
     * @param event
     */
    protected abstract fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?)

    /**
     * Called when the current event occurred when a gesture IS in progress. The
     * handling in this implementation may set the gesture out of progress (via
     * mGestureInProgress).
     * @param actionCode
     * @param event
     */
    protected abstract fun handleInProgressEvent(actionCode: Int, event: MotionEvent?)


    @SuppressLint("Recycle")
    protected open fun updateStateByEvent(event: MotionEvent?) {
        // Null Check
        if (event == null) return
        prevEvent?.let { prevEvent ->
            // Reset currentEvent
            currentEvent?.recycle()
            currentEvent = null
            currentEvent = MotionEvent.obtain(event)

            timeDelta = event.eventTime - prevEvent.eventTime

            // Pressure
            currentPressure = event.getPressure(event.actionIndex)
            prevPressure = prevEvent.getPressure(prevEvent.actionIndex)
        }
    }

    /**
     * Reset State Func.
     */
    protected open fun resetState() {
        prevEvent?.recycle()
        prevEvent = null
        currentEvent?.recycle()
        currentEvent = null

        gestureInProgress = false
    }
}