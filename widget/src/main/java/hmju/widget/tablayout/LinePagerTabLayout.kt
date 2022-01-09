package hmju.widget.tablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.Dimension
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.R
import hmju.widget.databinding.VChildLineTabLayoutBinding
import hmju.widget.extensions.currentItem
import hmju.widget.extensions.getFragmentActivity
import hmju.widget.extensions.initBinding
import hmju.widget.extensions.multiNullCheck

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
        private const val DEBUG = true
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
            }
        }
    }

    // [s] Attribute Set Variable
    @Dimension
    private val indicatorHeight: Float

    @Dimension
    private val indicatorCorner: Float
    private var scrollingOffset = 0

    @Dimension
    private val indicatorPadding: Float
    // [e] Attribute Set Variable

    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val tabContainer: hmju.widget.databinding.VLineTabLayoutBinding
    private val dataList: MutableList<PagerTabItem> by lazy { mutableListOf() }
    private val currentPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    var fixedSize: Int = -1

    private var tabCount: Int = 0
    private var currentPos: Int = 0 // 현재 위치값.
    private var posScrollOffset: Float = -1F // Scroll Offset.
    private var lastScrollX = 0
    private val indicatorRectF = RectF()
    private val bottomLineRectF = RectF()

    init {
        setWillNotDraw(false)

        context.obtainStyledAttributes(attrs, R.styleable.LinePagerTabLayout).run {
            val indicatorColor = getColor(
                R.styleable.LinePagerTabLayout_tabIndicatorColor,
                NO_ID
            )
            if (indicatorColor != NO_ID) {
                indicatorPaint.color = indicatorColor
            }

            indicatorHeight = getDimension(R.styleable.LinePagerTabLayout_tabIndicatorHeight, -1F)
            indicatorCorner = getDimension(R.styleable.LinePagerTabLayout_tabIndicatorCorner, 0F)
            indicatorPadding = getDimension(R.styleable.LinePagerTabLayout_tabIndicatorPadding, 0F)
            scrollingOffset =
                getDimensionPixelOffset(R.styleable.LinePagerTabLayout_tabScrollOffset, 0)
            recycle()
        }

        tabContainer = initBinding(R.layout.v_line_tab_layout, this)

        if (!isInEditMode) {
            this.getFragmentActivity()?.lifecycle?.addObserver(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentPosition.observe(this, {
            updateTab(it)
        })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (tabCount == 0 || indicatorHeight == -1F) return
        if (canvas == null) return

        drawBottomLine(canvas)
        drawLine(canvas, currentPos, posScrollOffset)
    }

    /**
     * DrawLine
     */
    private fun drawLine(canvas: Canvas, currPos: Int, scrollOffset: Float) {
        // 초기 Top or Bottom 셋팅
        if (indicatorRectF.top == 0F && height > 0) {
            indicatorRectF.top = height - indicatorHeight
            indicatorRectF.bottom = height.toFloat()
        }

        // Scroll 할때만 처리
        if (scrollOffset > 0F && currentPos < tabCount.minus(1)) {
            tabContainer.linerLayout.runCatching {
                multiNullCheck(
                    dataList[currPos].view,
                    dataList[currPos.plus(1)].view
                ) { currTab, nextTab ->
                    var left =
                        (scrollOffset * nextTab.left
                                + (1F - scrollOffset) * currTab.left)
                    var right =
                        (scrollOffset * nextTab.right
                                + (1F - scrollOffset) * currTab.right)
                    left += indicatorPadding
                    right -= indicatorPadding

                    indicatorRectF.left = left
                    indicatorRectF.right = right
                }
            }
        }

        // Timber.d("Draw Rect $indicatorRectF")
        // Indicator Draw
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

    private fun drawBottomLine(canvas: Canvas) {
        // BottomLine Draw
        if (bottomLineHeight > 0F) {
            canvas.drawRect(bottomLineRectF, bottomLinePaint)
            canvas.save()
        }
    }

    /**
     * Set Data List
     * @param list 데이터 리스트
     */
    fun setDataList(list: List<PagerTabItem>?) {
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

            initBinding<VChildLineTabLayoutBinding>(
                R.layout.v_child_line_tab_layout,
                this,
                false
            ) {
                this.lifecycleOwner = this@LinePagerTabLayout
                setVariable(hmju.widget.BR.listener, tabListener)
                setVariable(hmju.widget.BR.item, data)
                data.view = this.root

                // 특정 탭 레이아웃들 인디게이터 사이즈 처리
                if (index == 0) {
                    this.root.post {
                        indicatorRectF.left = this.root.left.plus(indicatorPadding)
                        indicatorRectF.right = this.root.right.minus(indicatorPadding)
                        this@LinePagerTabLayout.invalidate()
                    }
                }
                tabContainer.linerLayout.addView(this.root)
            }
        }

        dataList.clear()
        dataList.addAll(list)
    }

    /**
     * Update Tab Style.
     */
    private fun updateTab(pos: Int) {
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

    override fun onPageSelect(pos: Int) {}

    override fun onPageScroll(pos: Int, offset: Float) {
        currentPos = pos
        posScrollOffset = offset
        scrollToChild(pos, (offset * (tabContainer.linerLayout).getChildAt(pos).width).toInt())

        LogD("onPageScroll $currentPos ScrollOffset $posScrollOffset")

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
        if (ViewPager2.SCROLL_STATE_IDLE == state) {
            currentPosition.value = currentPos
        }
    }
}