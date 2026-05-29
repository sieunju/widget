package hmju.widget.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val trackRect = RectF()

    private var trackColorOn = "#34C759".toColorInt()
    private var trackColorOff = "#E5E5EA".toColorInt()
    private var thumbColor = Color.WHITE

    private var thumbMargin = 2.dp
    private var thumbSize = -1f  // -1 이면 track 높이 기준 자동 계산
    private var thumbRadius = 0f
    private var thumbCx = 0f
    private var animDuration = 250L
    private var touchPadding = 0f
    private val hitRect = Rect()

    private var checkedState = false
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    private val argbEvaluator = ArgbEvaluator()
    private var currentTrackColor = trackColorOff

    init {
        context.withStyledAttributes(attrs, R.styleable.CustomSwitchView) {
            trackColorOn = getColor(
                R.styleable.CustomSwitchView_switchTrackColorOn,
                trackColorOn
            )
            trackColorOff = getColor(
                R.styleable.CustomSwitchView_switchTrackColorOff,
                trackColorOff
            )
            thumbColor = getColor(
                R.styleable.CustomSwitchView_switchThumbColor,
                thumbColor
            )
            checkedState = getBoolean(
                R.styleable.CustomSwitchView_switchChecked,
                false
            )
            thumbSize = getDimension(R.styleable.CustomSwitchView_switchThumbSize, -1f)
            thumbMargin = getDimension(R.styleable.CustomSwitchView_switchThumbMargin, thumbMargin)
            animDuration = getInteger(R.styleable.CustomSwitchView_switchAnimDuration, 250).toLong()
            touchPadding = getDimension(R.styleable.CustomSwitchView_switchTouchPadding, 0f)
        }
        thumbPaint.color = thumbColor
        currentTrackColor = if (checkedState) trackColorOn else trackColorOff
        trackPaint.color = currentTrackColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val trackH = if (thumbSize > 0f) {
            (thumbSize + thumbMargin * 2).toInt()
        } else {
            31.dp.toInt()
        }
        val trackW = (trackH * 51f / 31f).toInt()
        val w = resolveSize(trackW, widthMeasureSpec)
        val h = resolveSize(trackH, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val trackH = if (thumbSize > 0f) thumbSize + thumbMargin * 2 else 31.dp
        val trackW = trackH * 51f / 31f
        val cx = w / 2f
        val cy = h / 2f
        trackRect.set(cx - trackW / 2f, cy - trackH / 2f, cx + trackW / 2f, cy + trackH / 2f)
        thumbRadius = if (thumbSize > 0f) thumbSize / 2f else (trackH / 2f) - thumbMargin
        thumbCx = if (checkedState) thumbCxOn() else thumbCxOff()
    }

    override fun onDraw(canvas: Canvas) {
        trackPaint.color = currentTrackColor
        val corner = trackRect.height() / 2f
        canvas.drawRoundRect(trackRect, corner, corner, trackPaint)
        canvas.drawCircle(thumbCx, trackRect.centerY(), thumbRadius, thumbPaint)
    }

    @SuppressLint("NewApi")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (touchPadding > 0f) {
            post {
                val p = parent as? ViewGroup ?: return@post
                getHitRect(hitRect)
                val expand = touchPadding.toInt()
                hitRect.inset(-expand, -expand)
                @Suppress("DrawAllocation")
                p.touchDelegate = TouchDelegate(hitRect, this)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (parent as? ViewGroup)?.touchDelegate = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        if (event.action == MotionEvent.ACTION_UP) {
            toggle()
        }
        return true
    }

    var isChecked: Boolean
        get() = checkedState
        set(value) {
            if (checkedState != value) toggle()
        }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }

    private fun toggle() {
        checkedState = !checkedState
        animateToState(checkedState)
        onCheckedChangeListener?.invoke(checkedState)
    }

    private fun animateToState(checked: Boolean) {
        val fromCx = thumbCx
        val toCx = if (checked) thumbCxOn() else thumbCxOff()
        val fromColor = currentTrackColor
        val toColor = if (checked) trackColorOn else trackColorOff

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animDuration
            // interpolator = OvershootInterpolator(1.5f)
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedFraction
                thumbCx = fromCx + (toCx - fromCx) * anim.animatedValue as Float
                currentTrackColor = argbEvaluator.evaluate(fraction, fromColor, toColor) as Int
                invalidate()
            }
            start()
        }
    }

    private fun thumbCxOff() = trackRect.left + thumbMargin + thumbRadius
    private fun thumbCxOn() = trackRect.right - thumbMargin - thumbRadius

    private val Int.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
}
