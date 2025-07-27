package hmju.widget.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * Description : Shadow 효과가 있는 CustomView
 * TODO 아직 테스트중입니다.
 * Created by juhongmin on 2025. 7. 6.
 */
class ShadowViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var shadowOffsetX = 0f
    private var shadowOffsetY = 0f
    private var shadowBlur = 0f
    private var shadowColor = Color.parseColor("#40000000")
    private var cornerRadius = 0f

    private var shadowView: ShadowView? = null

    init {
        // 커스텀 속성 읽기
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ShadowViewGroup,
            0, 0
        ).apply {
            try {
                shadowOffsetX = getDimension(R.styleable.ShadowViewGroup_shadowOffsetX, 0f)
                shadowOffsetY = getDimension(R.styleable.ShadowViewGroup_shadowOffsetY, 0f)
                shadowBlur = getDimension(R.styleable.ShadowViewGroup_shadowBlur, 0f)
                shadowColor = getColor(R.styleable.ShadowViewGroup_shadowColor, Color.parseColor("#40000000"))
                cornerRadius = getDimension(R.styleable.ShadowViewGroup_cornerRadius, 0f)
            } finally {
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addLastView()
    }

    private fun addLastView() {
        shadowView = ShadowView(context).apply {
            setShadowProperties(shadowOffsetX, shadowOffsetY, shadowBlur, shadowColor, cornerRadius)
        }
        addView(shadowView, 0) // 첫 번째 인덱스에 추가 (뒤쪽에 그려짐)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxWidth = 0
        var maxHeight = 0

        for (i in 1 until childCount) { // shadowView는 인덱스 0이므로 1부터
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxWidth = maxOf(maxWidth, child.measuredWidth)
            maxHeight = maxOf(maxHeight, child.measuredHeight)
        }

        // 그림자 크기 고려
        val totalWidth = maxWidth + shadowOffsetX.toInt() + shadowBlur.toInt()
        val totalHeight = maxHeight + shadowOffsetY.toInt() + shadowBlur.toInt()

        // 그림자 뷰 측정
        shadowView?.let {
            val shadowWidthSpec = MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY)
            val shadowHeightSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY)
            it.measure(shadowWidthSpec, shadowHeightSpec)
        }

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 그림자 뷰 레이아웃 (전체 크기)
        shadowView?.layout(0, 0, width, height)

        // 실제 자식 뷰들 레이아웃
        for (i in 1 until childCount) {
            val child = getChildAt(i)
            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }

    @Suppress("unused")
    fun updateShadow(offsetX: Float, offsetY: Float, blur: Float, color: Int) {
        shadowOffsetX = offsetX
        shadowOffsetY = offsetY
        shadowBlur = blur
        shadowColor = color

        shadowView?.setShadowProperties(offsetX, offsetY, blur, color, cornerRadius)
        requestLayout()
    }
}