package hmju.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.Collections

/**
 * Description : Custom SeekBar
 *
 * Created by juhongmin on 11/9/23
 */
// @Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomRangeSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {

        private const val TAG = "CustomRangeSlider"
        private const val DEBUG = true
        private fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private val thumbPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }
    private val fgPaint: Paint by lazy {
        Paint().apply {
            style = Style.FILL
        }
    }
    private val fgRect: RectF by lazy { RectF() }
    private val bgPaint: Paint by lazy {
        Paint().apply {
            style = Style.FILL
        }
    }
    private val bgRect: RectF by lazy { RectF() }
    private val thumbDrawable: GradientDrawable by lazy {
        GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(Color.BLACK, Color.BLACK)
        )
    }
    private var valueFrom = 85F
    private var valueTo = 130F
    private var thumbWidth = 30.dp
    private var thumbHeight = 30.dp
    private var trackHeight = 4.dp
    private var trackCorner = 4.dp
    private var currentPosition: Float = 0F
    private val values: MutableList<Float> by lazy { mutableListOf() } // 사이즈 2 고정
    private var startPosition : Float = -1F

    init {
        thumbDrawable.setSize(thumbWidth.toInt(), thumbHeight.toInt())
        values.add(85F)
        values.add(85F)
        bgPaint.color = Color.GRAY
        fgPaint.color = Color.BLACK
    }

    @SuppressLint("Recycle", "ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val x = event.x
        currentPosition = getPosition(x)
        updateValues()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                startPosition = currentPosition
            }

            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                startPosition = -1F
            }

            else -> {}
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTrack(canvas)
        // canvas.save()
        // canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(thumbHeight.toInt(), MeasureSpec.AT_MOST)
        )
        bgRect.left = thumbWidth / 2.0F
        bgRect.right = measuredWidth - (thumbWidth / 2.0F)
        bgRect.top = (measuredHeight / 2.0F) - trackHeight
        bgRect.bottom = (measuredHeight / 2.0F) + trackHeight
        fgRect.left = bgRect.left
        fgRect.top = bgRect.top
        fgRect.bottom = bgRect.bottom
    }

    private fun drawTrack(c: Canvas) {
        // Background Track Draw
        c.drawRoundRect(bgRect, trackCorner, trackCorner, bgPaint)
        // Foreground Track Draw
        val minPos = Collections.min(values)
        val maxPos = Collections.max(values)
        // 초기 아니면 둘다 시작점부터 있는 경우
        if (minPos == valueFrom && minPos == maxPos) {
            fgRect.left = bgRect.left
            fgRect.right = bgRect.left
        } else if (maxPos == valueTo && minPos == maxPos) {
            fgRect.left = bgRect.right
            fgRect.right = bgRect.right
        } else {
            // 시작한 위치값이 최소값보다 작은 경우
            if (minPos >= startPosition) {
                LogD("여기입니다. ")
                return
            }
            val minX = getPositionToX(minPos)
            val maxX = getPositionToX(maxPos)
            fgRect.left = Math.max(minX, bgRect.left)
            fgRect.right = Math.min(maxX, bgRect.right)
            if (fgRect.left > fgRect.right) {
                LogD("클때가 있습니다.")
                fgRect.right = bgRect.left
            }
            LogD("DrawTrack [${fgRect.left},${minX}]-[${fgRect.right},${maxX}]")
            LogD("Values $values  | ${minPos},$maxPos")
        }


        c.drawRoundRect(fgRect, trackCorner, trackCorner, fgPaint)
    }

    private fun drawThumb(c: Canvas) {

    }

    private fun getRange(): Float {
        return valueTo - valueFrom
    }

    private fun getAbsolutePosition(x: Float): Int {
        return (getRange() * x / width).toInt()
    }

    private fun getPosition(x: Float): Float {
        return (getRange() * x / width).plus(valueFrom)
    }

    private fun getPositionToX(pos: Float): Float {
        return if (pos == valueFrom) {
            width / getRange()
        } else {
            (width * pos.minus(valueFrom)) / getRange()
        }
    }

    private fun updateValues() {
        var min = Collections.min(values)
        var max = Collections.max(values)
        val diffLeft = Math.abs(min - currentPosition)
        val diffRight = Math.abs(max - currentPosition)

        if (diffLeft > diffRight) {
            max = currentPosition
        } else {
            min = currentPosition
        }
        values.clear()
        values.add(min)
        values.add(max)
        LogD("UpdateValues $values")
    }

    private val Int.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
}
