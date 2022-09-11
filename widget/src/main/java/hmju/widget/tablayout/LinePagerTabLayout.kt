package hmju.widget.tablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.postDelayed
import androidx.core.view.size
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.R
import hmju.widget.databinding.VLineTabLayoutBinding
import hmju.widget.extensions.currentItem
import hmju.widget.extensions.getFragmentActivity
import hmju.widget.extensions.initBinding
import hmju.widget.extensions.multiNullCheck

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

    @Dimension
    private var itemSide: Int = -1

    @Dimension
    private var itemDivider: Int = -1
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
                getDimensionPixelOffset(R.styleable.LinePagerTabLayout_tabScrollOffset, 0)

            itemSide = getDimension(R.styleable.LinePagerTabLayout_tabItemSide, 0F).toInt()
            itemDivider = getDimension(R.styleable.LinePagerTabLayout_tabItemDivider, 0F).toInt()

            recycle()
        }

        tabContainer = initBinding(R.layout.v_line_tab_layout, this)

        // Preview Mode
        if (isInEditMode) {
            val previewTabItems: String
            val previewTabPos: Int

            context.obtainStyledAttributes(attrs, R.styleable.LinePagerTabLayout).run {
                previewTabItems = getString(R.styleable.LinePagerTabLayout_tabPreviewItems) ?: ""
                previewTabPos = getInt(R.styleable.LinePagerTabLayout_tabPreviewPos, 0)
                recycle()
            }
            addPreviewTabItems(previewTabItems, previewTabPos)
        } else {
            this.getFragmentActivity()?.lifecycle?.addObserver(this)
        }
    }

    // [s] Preview Mode Perform

    /**
     * 미리보기 지원 함수
     */
    private fun addPreviewTabItems(tabItems: String, pos: Int) {
        try {
            val split = tabItems.split(",")
            if (type == Type.FIXED) {
                tabContainer.linerLayout.weightSum = split.size.toFloat()
            }
            split.forEachIndexed { index, s ->
                tabContainer.linerLayout.addView(initPreview(s, index, pos))
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    /**
     * 미리보기 화면에 대한 표시 처리
     */
    private fun initPreview(title: String, currPos: Int, indicatorPos: Int): ViewGroup {
        return ConstraintLayout(context).apply {
            // Scrollable Type 에 따라서 Child View 처리
            layoutParams = if (type == Type.FIXED) {
                LinearLayoutCompat.LayoutParams(
                    0,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).also {
                    it.weight = 1F
                }
            } else {
                LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
                )
            }

            addView(initPreviewTextView(title, currPos == indicatorPos))
            if (indicatorHeight > 0F || bottomLineHeight > 0F) {
                addView(initPreviewIndicator(currPos == indicatorPos))
            }
        }
    }

    private fun initPreviewTextView(title: String, isIndicator: Boolean): AppCompatTextView {
        return AppCompatTextView(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                it.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                it.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                it.leftMargin = itemDivider
                it.rightMargin = itemDivider
            }
            gravity = Gravity.CENTER

            if (isIndicator) {
                TextViewCompat.setTextAppearance(this, enableStyle)
            } else {
                TextViewCompat.setTextAppearance(this, disableStyle)
            }

            text = title
        }
    }

    private fun initPreviewIndicator(isIndicator: Boolean): View {
        return View(context).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                if (isIndicator) indicatorHeight.toInt() else bottomLineHeight.toInt()
            ).also {
                it.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                it.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            setBackgroundColor(if (isIndicator) indicatorPaint.color else bottomLinePaint.color)
        }
    }
    // [e] Preview Mode Perform

    override fun onCreate() {
        super.onCreate()
        currentPosition.observe(this) { updateTab(it) }
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
            data.enableTextStyle = enableStyle
            data.disableTextStyle = disableStyle
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
                    addEmptyView(
                        tabContainer.linerLayout,
                        index,
                        if (index == 0) itemSide else itemDivider
                    )
                }
                // [e] Left Decoration

                // 맵 저장
                positionMap[index] = tabContainer.linerLayout.childCount
                tabContainer.linerLayout.addView(this.root)


                // [s] Left Decoration
                if (type == Type.SCROLLABLE) {
                    addEmptyView(
                        tabContainer.linerLayout,
                        index,
                        if (index == tabCount - 1) itemSide else itemDivider
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
        if (requestWidth == -1) return
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
        try {
            if (tabContainer.linerLayout.size > pos) {
                val scrollOffset = offset * (tabContainer.linerLayout).getChildAt(pos).width
                scrollToChild(pos, scrollOffset.toInt())
            }
        } catch (ex: Exception) {
            // ignore
        }

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
        if (ViewPager2.SCROLL_STATE_IDLE == state) {
            currentPosition.value = currentPos
            setCurrentIndicator(currentPos)
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

    /**
     * Indicator Update
     */
    fun updateLineIndicator() {
        postDelayed(300) {
            posScrollOffset = 0.0000001F
            invalidate()
        }
    }

    fun moveTab(pos: Int) {
        currentPos = pos
        updateTab(pos)
        setCurrentIndicator(pos)
    }

    /**
     * 특정 위치에 있는 타이틀값 가져와서 수정하고 싶을때 사용하는 함수
     * @param pos 특정 위치값
     */
    fun getTabTextView(pos: Int): AppCompatTextView? {
        return if (dataList.size > pos) {
            val tabItem = dataList[pos]
            try {
                tabItem.view?.findViewById<AppCompatTextView>(R.id.tvTitle)
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }
}
