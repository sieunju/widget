package hmju.widget.gesture.decetor

import android.annotation.SuppressLint
import android.graphics.PointF
import android.view.MotionEvent

/**
 * Description : Move Gesture Detector
 *
 * Created by juhongmin on 11/21/21
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MoveGestureDetector(
    private val listener:
    OnMoveGestureListener
) : BaseGestureDetector() {

    interface OnMoveGestureListener {
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector): Boolean
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    companion object {
        open class SimpleOnMoveGestureListener : OnMoveGestureListener {
            override fun onMove(detector: MoveGestureDetector) = false

            override fun onMoveBegin(detector: MoveGestureDetector) = true

            override fun onMoveEnd(detector: MoveGestureDetector) {}
        }

        val FOCUS_DELTA_ZERO = PointF()
    }

    private lateinit var currentFocusInternal: PointF
    private lateinit var prevFocusInternal: PointF
    lateinit var currentFocus: PointF
    private val focusExternal = PointF()

    @SuppressLint("Recycle")
    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?) {
        // Null Check
        if (event == null) return

        when (actionCode) {
            MotionEvent.ACTION_DOWN -> {
                resetState()

                prevEvent = MotionEvent.obtain(event)
                timeDelta = 0

                updateStateByEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                gestureInProgress = listener.onMoveBegin(this)
            }
        }
    }

    @SuppressLint("Recycle")
    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL
            -> {
                listener.onMoveEnd(this)
                resetState()
            }

            MotionEvent.ACTION_MOVE -> {
                event?.let {
                    updateStateByEvent(it)
                }

                if (currentPressure / prevPressure > PRESSURE_THRESHOLD) {
                    val updatePrevious = listener.onMove(this)
                    if (updatePrevious) {
                        prevEvent?.recycle()
                        prevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent?) {
        super.updateStateByEvent(event)
        // Null Check
        if (event == null) return

        prevEvent?.let { prev ->
            // Focus internal
            currentFocusInternal = determineFocalPoint(event)
            prevFocusInternal = determineFocalPoint(prev)

            // Focus external
            // - Prevent skipping of focus delta when a finger is added or removed
            val skipNextMoveEvent: Boolean = prev.pointerCount != event.pointerCount
            currentFocus = if (skipNextMoveEvent) FOCUS_DELTA_ZERO
            else PointF(
                currentFocusInternal.x - prevFocusInternal.x,
                currentFocusInternal.y - prevFocusInternal.y
            )
        }

        focusExternal.x += currentFocus.x
        focusExternal.y += currentFocus.y
    }


    private fun determineFocalPoint(e: MotionEvent): PointF {
        val pCount = e.pointerCount
        var x = 0F
        var y = 0F

        for (i in 0 until pCount) {
            x += e.getX(i)
            y += e.getY(i)
        }

        return PointF(x / pCount, y / pCount)
    }
}