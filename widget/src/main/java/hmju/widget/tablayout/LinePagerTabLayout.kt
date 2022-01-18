package hmju.widget.tablayout

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Dimension
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.R
import hmju.widget.databinding.VLineTabLayoutBinding
import hmju.widget.extensions.*

/**
 * LinePagerTabLayout
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
                viewPager?.currentItem(pos, true) ?: run {
                    setCurrentIndicator(currentPos)
                    updateTab(currentPos)
                }
            }

            tabClickListener?.onTabClick(pos)
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
    private val side: Int
    private val divider: Int
    // [e] Attribute Set Variable

    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val tabContainer: VLineTabLayoutBinding
    private val dataList: MutableList<PagerTabItem> by lazy { mutableListOf() }
    private val currentPosition: MutableLiveData<Int> by lazy { MutableLiveData() }

    var fixedSize: Int = -1

    private var tabCount: Int = 0
    private var currentPos: Int = 0 // 현재 위치값.
    private var posScrollOffset: Float = -1F // Scroll Offset.
    private var lastScrollX = 0
    private val indicatorRectF = RectF()
    private val bottomLineRectF = RectF()

    var tabClickListener: TabClickListener? = null

    // Key Tab 을 표시하는 포지션 Value LinearLayout 에 추가된 포지션 값
    private val positionMap: MutableMap<Int, Int> by lazy { mutableMapOf() }

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
                getDimensionPixelOffset(R.styleable.LinePagerTabLayout_tabScrollOffset, 30.dp)
            side = getDimensionPixelOffset(R.styleable.LinePagerTabLayout_tabLineSideSpace, 0)
            divider = getDimensionPixelOffset(R.styleable.LinePagerTabLayout_tabLineDivider, 0)
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
     * Bottom Line 그리기
     * @param canvas Canvas
     */
    private fun drawBottomLine(canvas: Canvas) {
        // BottomLine Draw
        if (bottomLineHeight > 0F) {
            // 초기값 셋팅
            if (bottomLineRectF.height() == 0F) {
                bottomLineRectF.left = 0F
                bottomLineRectF.top = height - bottomLineHeight
                bottomLineRectF.bottom = height.toFloat()
            }
            bottomLineRectF.right = tabContainer.linerLayout.right.toFloat()
            canvas.drawRect(bottomLineRectF, bottomLinePaint)
            canvas.save()
        }
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
                    left -= indicatorPadding
                    right += indicatorPadding

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

    /**
     * Set Data List
     * @param list 데이터 리스트
     */
    fun setDataList(list: List<PagerTabItem>?) {
        if (list == null) return

        // [s] 초기화 처리
        currentPos = 0
        currentPosition.value = 0
        tabCount = list.size
        tabContainer.linerLayout.removeAllViews()
        positionMap.clear()
        // [e] 초기화 처리

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
            initBinding<hmju.widget.databinding.VChildDefaultTabLayoutBinding>(
                R.layout.v_child_default_tab_layout,
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
                        indicatorRectF.left = this.root.left.minus(indicatorPadding)
                        indicatorRectF.right = this.root.right.plus(indicatorPadding)
                        this@LinePagerTabLayout.invalidate()
                    }
                }

                // [s] Left Decoration
                if (type == Type.SCROLLABLE) {
                    addEmptyView(tabContainer.linerLayout, index, if (index == 0) side else divider)
                }
                // [e] Left Decoration

                // 맵 저장
                positionMap.put(index, tabContainer.linerLayout.childCount)
                tabContainer.linerLayout.addView(this.root)


                // [s] Left Decoration
                if (type == Type.SCROLLABLE) {
                    addEmptyView(
                        tabContainer.linerLayout,
                        index,
                        if (index == tabCount - 1) side else divider
                    )
                }
                // [e] Left Decoration
            }
        }

        dataList.clear()
        dataList.addAll(list)
    }

    /**
     * 빈 뷰 추가 하기
     * 클릭 리스너는 추가한 인덱스 클릭 리스너 추라
     */
    private fun addEmptyView(rootView: LinearLayoutCompat, idx: Int, requestWidth: Int) {
        View(context).apply {
            layoutParams = LayoutParams(requestWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            setOnClickListener {
                tabListener.onTabClick(idx, it)
            }
            rootView.addView(this)
        }
    }

    /**
     * Update Tab Style.
     */
    private fun updateTab(pos: Int) {
        dataList.forEach { data ->
            data.isSelected?.value = data.pos == pos
        }
    }

    override fun onPageSelect(pos: Int) {}

    override fun onPageScroll(pos: Int, offset: Float) {
        currentPos = pos
        posScrollOffset = offset

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
        if (ViewPager2.SCROLL_STATE_IDLE == state) {
            currentPosition.value = currentPos
            setCurrentIndicator(currentPos)

            dataList[currentPos].view?.let {
                ObjectAnimator.ofInt(
                    this,
                    "scrollX",
                    it.left - scrollingOffset
                ).apply {
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                }.start()
            }
        }
    }

    /**
     * 현재 포지션값에 따라서 인디게이터 처리
     * @param pos TargetPosition
     */
    private fun setCurrentIndicator(pos: Int) {
        runCatching {
            val childView = dataList[pos].view
            if (childView != null) {
                indicatorRectF.left = childView.left - indicatorPadding
                indicatorRectF.right = childView.right + indicatorPadding
                invalidate()
            }
        }
    }

    fun getCurrentPosition() = currentPos
}