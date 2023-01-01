package hmju.widget.viewpager.indicator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.viewpager.R

/**
 * Description : Dots Indicator
 * Width 는 LayoutParams.MATCH_PARENT 로 하는걸 지향합니다.
 *
 * Created by juhongmin on 2022/12/27
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class DotIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    internal class Model {
        var corner = -1F
        val rect: Rect by lazy { Rect() }
        val paint: Paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
        }
    }

    enum class Gravity {
        LEFT, CENTER, RIGHT
    }

    private val disableModel: Model by lazy { Model() } // 비활성화
    private val enableModel: Model by lazy { Model() }

    var viewPager: ViewPager2? = null
        set(value) {
            if (value != null) {
                value.unregisterOnPageChangeCallback(pageListener)
                value.registerOnPageChangeCallback(pageListener)
            }
            field = value
        }

    private var currentPos: Int = 0

    // [s] Attribute Set Variable
    private var gravity: Gravity = Gravity.LEFT
    private var isInfinite: Boolean = false
    private var divider: Float = 0F
    // [e] Attribute Set Variable

    init {
        setWillNotDraw(false)

        context.obtainStyledAttributes(attrs, R.styleable.DotIndicator).run {
            isInfinite = getBoolean(R.styleable.DotIndicator_dotIndicatorIsInfinite, false)
            var size = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorEnableSize, 50)
            enableModel.apply {
                corner = getDimension(R.styleable.DotIndicator_dotIndicatorEnableCorner, -1F)
                paint.color =
                    getColor(R.styleable.DotIndicator_dotIndicatorEnableColor, Color.BLACK)
                rect.right = size
                rect.bottom = size
            }
            size = getDimensionPixelSize(R.styleable.DotIndicator_dotIndicatorDisableSize, 50)
            disableModel.apply {
                corner = getDimension(R.styleable.DotIndicator_dotIndicatorDisableCorner, -1F)
                paint.color =
                    getColor(R.styleable.DotIndicator_dotIndicatorDisableColor, Color.WHITE)
                rect.right = size
                rect.bottom = size
            }

            divider = getDimension(R.styleable.DotIndicator_dotIndicatorDivider, 0F)
            gravity = Gravity.values()[getInt(R.styleable.DotIndicator_dotIndicatorGravity, 0)]
            recycle()
        }

        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            val maxHeight = getExactlyMaxHeight()
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val height = height
        // 위아래 간격 맞추기 함수
        // Disable Model
        if (height != disableModel.rect.height()) {
            val margin = (height - disableModel.rect.height()) / 2
            val modelHeight = disableModel.rect.height()
            disableModel.rect.top = margin
            disableModel.rect.bottom = margin.plus(modelHeight)
        }
        // Enable Model
        if (height != enableModel.rect.height()) {
            val margin = (height - enableModel.rect.height()) / 2
            val modelHeight = enableModel.rect.height()
            enableModel.rect.top = margin
            enableModel.rect.bottom = margin.plus(modelHeight)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        drawDots(canvas)
    }

    /**
     * 활성화, 비활성화 기준 최대값 + 위아래 패딩값
     * Canvas 에서 표현할 최대 높이값
     */
    private fun getExactlyMaxHeight(): Int {
        var maxHeight = disableModel.rect.height().coerceAtLeast(enableModel.rect.height())
        maxHeight += paddingTop
        maxHeight += paddingBottom
        return maxHeight
    }

    private fun drawDots(canvas: Canvas) {
        when (gravity) {
            Gravity.LEFT -> drawLeftDots(canvas)
            Gravity.CENTER -> drawCenterDots(canvas)
            Gravity.RIGHT -> drawRightDots(canvas)
        }
    }

    /**
     * Dots Indicator Draw 처리 함수
     * 왼쪽 정렬 타입
     */
    private fun drawLeftDots(canvas: Canvas) {
        val position = getRealPosition(currentPos)
        var startPoint = paddingLeft
        val count = getRealItemCount()
        for (idx in 0 until count) {
            // Disable Dots
            startPoint += if (idx != position) {
                drawDots(canvas, startPoint, disableModel)
                disableModel.rect.width()
            } else {
                // Enable Dots
                drawDots(canvas, startPoint, enableModel)
                enableModel.rect.width()
            }
            startPoint += divider.toInt()
        }
    }

    /**
     * Dots Indicator Draw 처리 함수
     * 가운데 정렬 타입
     */
    private fun drawCenterDots(canvas: Canvas) {
        val position = getRealPosition(currentPos)
        val contentsWidth = getContentsWidth()
        var startPoint = (width - contentsWidth) / 2
        val count = getRealItemCount()
        for (idx in 0 until count) {
            // Disable Dots
            startPoint += if (idx != position) {
                drawDots(canvas, startPoint, disableModel)
                disableModel.rect.width()
            } else {
                // Enable Dots
                drawDots(canvas, startPoint, enableModel)
                enableModel.rect.width()
            }
            startPoint += divider.toInt()
        }
    }

    /**
     * Dots Indicator Draw 처리 함수
     * 오른쪽 타입
     */
    private fun drawRightDots(canvas: Canvas) {
        val position = getRealPosition(currentPos)
        var startPoint = width - getContentsWidth().plus(paddingRight)
        val count = getRealItemCount()
        for (idx in 0 until count) {
            // Disable Dots
            startPoint += if (idx != position) {
                drawDots(canvas, startPoint, disableModel)
                disableModel.rect.width()
            } else {
                // Enable Dots
                drawDots(canvas, startPoint, enableModel)
                enableModel.rect.width()
            }
            startPoint += divider.toInt()
        }
    }

    /**
     * Dot 그리기 함수
     * @param canvas Canvas
     * @param point 표시할 좌표
     * @param model Enable / Disable
     */
    private fun drawDots(canvas: Canvas, point: Int, model: Model) {
        val pointRect = Rect()
        pointRect.top = model.rect.top
        pointRect.bottom = model.rect.bottom
        pointRect.left = point
        pointRect.right = point.plus(model.rect.width())
        if (model.corner > 0F) {
            canvas.drawRoundRect(RectF(pointRect), model.corner, model.corner, model.paint)
        } else {
            canvas.drawRect(pointRect, model.paint)
        }
    }

    /**
     * 실제로 사용할 너비값 구하기
     * 선택됐을때 Dots Width, 선택되지 않는 Dots Width, 사이 간격
     */
    private fun getContentsWidth(): Int {
        val enableWidth = enableModel.rect.width()
        val disableWidth = (disableModel.rect.width() * getRealItemCount().minus(1)
            .coerceAtLeast(0))
        val dividerSize = divider * getRealItemCount().minus(1)
            .coerceAtLeast(0)
        return (enableWidth + disableWidth + dividerSize).toInt()
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
                    getRealItemCount() - 1
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

        override fun onPageSelected(pos: Int) {
            currentPos = pos
            invalidate()
        }
    }
}
