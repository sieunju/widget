package hmju.widget.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.R

/**
 * Description : LineIndicator
 *
 * Created by juhongmin on 2022/01/10
 */
class LineIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LineIndicator"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    enum class Type {
        FILL, UNIT
    }

    private val indicatorRectF: RectF by lazy { RectF() }
    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }
    var viewPager: ViewPager2? = null
        set(value) {
            if (value != null) {
                value.unregisterOnPageChangeCallback(pageListener)
                value.registerOnPageChangeCallback(pageListener)
            }
            field = value
        }
    var size: Int = 0
        set(value) {
            LogD("Width $width")
            if (width > 0) {
                unitWidth = (width.toFloat() / value.toFloat()).toInt()
                LogD("Unit Width $unitWidth")
            }
            field = value
        }
    private var unitWidth: Int = 0
    private var currentPos: Int = 0
    private var posScrollOffset: Float = -1F

    // [s] Attribute Set Variable
    protected var type: Type = Type.FILL
    protected var isInfinite: Boolean = false
    // [e] Attribute Set Variable

    init {
        setWillNotDraw(false)

        context.obtainStyledAttributes(attrs, R.styleable.LineIndicator).run {
            type = Type.values()[getInt(R.styleable.LineIndicator_lineIndicatorType, 0)]
            isInfinite = getBoolean(R.styleable.LineIndicator_lineIndicatorIsInfinite, false)
            indicatorPaint.color =
                getColor(R.styleable.LineIndicator_lineIndicatorColor, Color.BLACK)
            setBackgroundColor(
                getColor(
                    R.styleable.LineIndicator_lineIndicatorBgColor,
                    Color.LTGRAY
                )
            )
            recycle()
        }

        post {
            indicatorRectF.top = 0F
            indicatorRectF.left = 0F
            indicatorRectF.bottom = height.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || size == 0) return

        if (indicatorRectF.top == 0F && height > 0) {
            indicatorRectF.top = 0F
            indicatorRectF.bottom = height.toFloat()
        }

        // Unit Width 설정
        if (unitWidth == 0) {
            unitWidth = (width.toFloat() / size.toFloat()).toInt()
            LogD("onDraw Width $unitWidth")
        }

        if (type == Type.FILL) {
            drawIndicatorFill(canvas, currentPos, posScrollOffset)
        } else {
            drawIndicatorUnit(canvas, currentPos, posScrollOffset)
        }
    }

    private fun drawIndicatorFill(canvas: Canvas, pos: Int, offset: Float) {

    }

    private fun drawIndicatorUnit(canvas: Canvas, pos: Int, offset: Float) {
        if(pos < size.minus(1)) {
            val nextPos = pos.plus(1)
            val left = (offset * (unitWidth * nextPos)) + ((1F - offset) * unitWidth * pos)
            val right = (offset * (unitWidth))
        }

    }

    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            pos: Int,
            offset: Float,
            offsetPixel: Int
        ) {
            if (offset < 1F) {
                currentPos = pos
                posScrollOffset = offset

                // ReDraw
                invalidate()
            }
        }
    }
}