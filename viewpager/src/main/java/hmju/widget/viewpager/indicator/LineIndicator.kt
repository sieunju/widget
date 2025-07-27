package hmju.widget.viewpager.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.viewpager.R

/**
 * Description : LineIndicator
 *
 * Created by juhongmin on 2022/01/10
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class LineIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LineIndicator"
        private const val DEBUG = false
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

    private var unitWidth: Int = 0
    private var currentPos: Int = 0
    private var posScrollOffset: Float = -1F

    // [s] Attribute Set Variable
    private var type: Type = Type.FILL
    var isInfinite: Boolean = false

    @Dimension
    private val indicatorCorner: Float
    // [e] Attribute Set Variable

    init {
        setWillNotDraw(false)

        context.obtainStyledAttributes(attrs, R.styleable.LineIndicator).run {
            type = Type.values()[getInt(R.styleable.LineIndicator_lineIndicatorType, 0)]
            isInfinite = getBoolean(R.styleable.LineIndicator_lineIndicatorIsInfinite, false)
            indicatorPaint.color =
                getColor(R.styleable.LineIndicator_lineIndicatorColor, Color.BLACK)
            indicatorCorner = getDimension(R.styleable.LineIndicator_lineIndicatorCorner, 0F)
            val bgColor = getColor(R.styleable.LineIndicator_lineIndicatorBgColor, Color.LTGRAY)

            // BG 도 코너 맥이기
            if (indicatorCorner > 0) {
                background = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(bgColor, bgColor)
                ).apply {
                    cornerRadius = indicatorCorner
                }
            } else {
                setBackgroundColor(bgColor)
            }

            recycle()
        }

        clipToOutline = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (getRealItemCount() == 0) return

        if (indicatorRectF.top == 0F && height > 0) {
            indicatorRectF.top = 0F
            indicatorRectF.bottom = height.toFloat()
        }

        // Unit Width 설정
        if (unitWidth <= 0) {
            unitWidth = (width.toFloat() / getRealItemCount().toFloat()).toInt()
        }

        if (type == Type.FILL) {
            computeIndicatorFill(getRealPosition(currentPos), posScrollOffset)
        } else {
            computeIndicatorUnit(getRealPosition(currentPos), posScrollOffset)
        }

        drawIndicator(canvas)
    }

    /**
     * 인디게이터 가득 채우는 타입 게산
     * 계산후 @see [indicatorRectF] 에 값을 셋팅한다.
     * @param pos Target Position
     * @param offset 스크롤 양
     */
    private fun computeIndicatorFill(pos: Int, offset: Float) {
        val nextPos = pos.plus(1)
        val currRight = (unitWidth * pos) + unitWidth
        val nextRight = (unitWidth * nextPos) + unitWidth
        val right = (offset * nextRight) + ((1F - offset) * currRight)
        indicatorRectF.right = right
    }

    /**
     * 인디게이터 단위로 채우는 타입 계산
     * 계산후 @see [indicatorRectF] 에 값을 셋팅 한다.
     * @param pos Target Position
     * @param offset 스크롤 양
     */
    private fun computeIndicatorUnit(pos: Int, offset: Float) {
        val nextPos = pos.plus(1)
        val currLeft = unitWidth * pos
        val currRight = (unitWidth * pos) + unitWidth
        val nextLeft = unitWidth * nextPos
        val nextRight = (unitWidth * nextPos) + unitWidth
        val left = (offset * nextLeft) + ((1F - offset) * currLeft)
        val right = (offset * nextRight) + ((1F - offset) * currRight)
        indicatorRectF.left = left
        indicatorRectF.right = right
    }

    /**
     * 인디게이터 그리기
     * @param canvas
     */
    private fun drawIndicator(canvas: Canvas) {
        if (indicatorCorner > 0) {
            canvas.drawRoundRect(
                indicatorRectF,
                indicatorCorner,
                indicatorCorner,
                indicatorPaint
            )
        } else {
            canvas.drawRect(
                indicatorRectF,
                indicatorPaint
            )
        }
    }

    /**
     * 무한 롤링인 경우 실제 포지션 값으로 리턴처리하는 함수
     * 무한 롤링 ViewPager2 구조
     * Fake LastIndex | RealIndex | Fake FirstIndex
     * @param pos Target Position
     */
    private fun getRealPosition(pos: Int): Int {
        var index = pos
        if (isInfinite) {
            index = when (pos) {
                0 -> {
                    // Fake LastIndex
                    getRealItemCount()
                }

                getRealItemCount() + 1 -> {
                    // Fake FirstIndex
                    0
                }

                else -> {
                    pos.minus(1)
                }
            }
        }
        return index
    }

    /**
     * ViewPager2 Item Count
     */
    private fun getRealItemCount(): Int {
        var itemCount = viewPager?.adapter?.itemCount ?: 0
        if (isInfinite) {
            itemCount = itemCount.minus(2)
        }
        return itemCount
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
