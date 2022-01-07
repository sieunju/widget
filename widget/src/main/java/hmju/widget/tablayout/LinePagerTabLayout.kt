package hmju.widget.tablayout

import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.Dimension
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import hmju.widget.R
import hmju.widget.BR
import hmju.widget.extensions.currentItem
import hmju.widget.extensions.initBinding

/**
 * Description : Line Pager TabLayout
 *
 * Created by juhongmin on 2021/12/31
 */
class LinePagerTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseTabLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LinePagerTabLayout"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private val tabListener = object : Listener {
        override fun onTabClick(pos: Int, view: View) {
            if (!isEnabled) {
                return
            }
            if (currentPos != pos) {
                currentPos = pos
                // ViewPager Animation 인경우
                viewPager?.currentItem(pos, true)
                updateTab(pos)
            }
        }
    }

    @Dimension
    private var indicatorHeight: Float = -1F
    private var scrollingOffset = 0

    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val tabContainer: hmju.widget.databinding.VLineTabLayoutBinding
    private val dataList: MutableList<PagerTabItem> by lazy { mutableListOf() }

    var fixedSize: Int = -1

    private var tabCount: Int = 0
    private var currentPos: Int = 0 // 현재 위치값.
    private var posScrollOffset: Float = -1F // Scroll Offset.
    private var lastScrollX = 0

    init {
        setWillNotDraw(false)

        // 속성 값 세팅
        attrs?.run {
            val attr: TypedArray =
                context.obtainStyledAttributes(this, R.styleable.LinePagerTabLayout)
            try {
                val color = attr.getColor(
                    R.styleable.LinePagerTabLayout_tabIndicatorColor,
                    NO_ID
                )
                if (color != NO_ID) {
                    indicatorPaint.color = color
                }
                indicatorHeight =
                    attr.getDimension(R.styleable.LinePagerTabLayout_tabIndicatorHeight, -1F)
                scrollingOffset = attr.getDimensionPixelOffset(
                    R.styleable.LinePagerTabLayout_tabScrollOffset,
                    0
                )
            } finally {
                attr.recycle()
            }
        }

        tabContainer = initBinding(R.layout.v_line_tab_layout, this)

        if (bottomLineHeight == NO_ID) {
            tabContainer.isLine = false
        } else {
            tabContainer.isLine = true
            tabContainer.lineHeight = bottomLineHeight
        }

        if (!isInEditMode) {
            // DefaultActivity
            if (context is FragmentActivity) {
                context.lifecycle.addObserver(this)
            } else {
                val baseContext = (context as ContextWrapper).baseContext
                if (baseContext is FragmentActivity) {
                    baseContext.lifecycle.addObserver(this)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (tabCount == 0 || indicatorHeight == -1F) return

        // RootView 에서 Draw 해서 표현
        with(tabContainer.linerLayout) {
            val currentTab: View? =
                if (this.childCount > currentPos) getChildAt(currentPos) else null
            var lineLeft: Float = currentTab?.left?.toFloat() ?: 0F
            var lineRight: Float = currentTab?.right?.toFloat() ?: 0F

            LogD("LineLeft $lineLeft LineRight $lineRight Offset ${posScrollOffset}")

            // Scroll 하는 경우 Indicator 자연스럽게 넘어가기 위한 로직.
            if (posScrollOffset > 0F && currentPos < tabCount - 1) {
                val nextTab = getChildAt(currentPos + 1)
                lineLeft = (posScrollOffset * nextTab.left + (1F - posScrollOffset) * lineLeft)
                lineRight = (posScrollOffset * nextTab.right + (1F - posScrollOffset) * lineRight)
            }

            canvas?.drawRect(
                lineLeft,
                height - indicatorHeight,
                lineRight,
                height.toFloat(),
                indicatorPaint
            )
        }
    }

    fun setDataList(childLayoutType: PagerTabType, list: List<PagerTabItem>?) {
        if (list == null) return

        tabCount = list.size
        tabContainer.linerLayout.removeAllViews()
        val layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT
        )

        if (type == Type.FIXED) {
            if (fixedSize != -1) {
                tabContainer.linerLayout.weightSum = fixedSize.toFloat()
            } else {
                tabContainer.linerLayout.weightSum = tabCount.toFloat()
            }
            layoutParams.width = 0
            layoutParams.weight = 1F
        }

        // 데이터 리스트에 맞게 아이템 Binding 한다.
        list.forEachIndexed { index, data ->
            data.pos = index
            data.isSelected?.value = index == currentPos
            data.txtColor = enableTxtColor
            data.disableTxtColor = disableTxtColor

            data.txtSize = textSize
            data.isChangeTextStyle = isChangeTextStyle

            val itemBinding = initBinding<ViewDataBinding>(
                childLayoutType.layoutId,
                this,
                false
            ) {
                setVariable(BR.listener, tabListener)
                setVariable(BR.item, data)
            }

            (itemBinding.root as LinearLayoutCompat).layoutParams = layoutParams
            tabContainer.linerLayout.addView(itemBinding.root)

            // Redraw
            invalidate()
        }
        dataList.clear()
        dataList.addAll(list)
    }

    /**
     * Update Tab Style.
     */
    fun updateTab(pos: Int) {
        dataList.forEach { data ->
            data.isSelected?.value = data.pos == pos
        }
    }

    /**
     * 한 화면에 보여지는 Tab 이 Over 되는 경우
     * Scrolling 처리 함수.
     * @param pos Current Pos
     * @param offset Scrolling Offset
     */
    private fun scrollToChild(pos: Int, offset: Int) {
        if (tabCount == 0) return

        var newScrollX = tabContainer.linerLayout.getChildAt(pos).left + offset

        if (pos > 0 || offset > 0) {
            newScrollX -= scrollingOffset
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            scrollTo(newScrollX, 0)
        }
    }

    override fun onPageSelect(pos: Int) {
        LogD("onPageSelect $pos CurrentPos $currentPos")

        updateTab(pos)
    }

    override fun onPageScroll(pos: Int, offset: Float) {
        currentPos = pos
        posScrollOffset = offset
        scrollToChild(pos, (offset * (tabContainer.linerLayout).getChildAt(pos).width).toInt())

        LogD("onPageScroll $currentPos ScrollOffset $posScrollOffset")

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
//        if (ViewPager2.SCROLL_STATE_IDLE == state) {
//            updateTab(currentPos)
//        }
    }
}