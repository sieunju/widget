package hmju.widget.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.time.format.TextStyle

/**
 * Description : Rolling Number Animation TextView
 *
 * Created by juhongmin on 2025. 7. 9.
 */
class RollingAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "RollingAmountViewV2"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private var currentAmount = 0L
    private var amountTextSize: Float = 20f
    private var amountTextSideSpan: Int = 6.dp

    private val amountRootView: LinearLayout by lazy {
        LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).also {
                it.topToTop = LayoutParams.PARENT_ID
                it.endToEnd = LayoutParams.PARENT_ID
                it.bottomToBottom = LayoutParams.PARENT_ID
            }
            clipToPadding = false
        }
    }

    init {
        addView(amountRootView)
        amountTextSize = 14f
    }

    fun setAmount(amount: Long) {
        amountRootView.removeAllViews()
        val amountArr = NumberFormat.getNumberInstance()
            .format(amount)
            .map { char ->
                if (char == ',') {
                    -1
                } else {
                    char.digitToInt()
                }
            }
        var delay = 0L
        amountArr.forEach { digits ->
            if (digits == -1) {
                val tvComma = initDigitsTextView(0)
                tvComma.text = ","
                tvComma.alpha = 0f
                tvComma.translationY = 50f
                amountRootView.addView(tvComma)
                tvComma.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(delay + 70)
                    .start()
                delay += 70
                return@forEach
            }
            val snapHelper = LinearSnapHelper()
            val adapter = DigitsAdapter()
            val scroller = CustomSmoothScroller(currentAmount < amount)
            val rv = SingleItemRecyclerView(context).also {
                it.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                it.adapter = adapter
                it.isNestedScrollingEnabled = false
                it.clipToPadding = false
                it.alpha = 0F
                snapHelper.attachToRecyclerView(it)
            }

            amountRootView.addView(rv)
            postDelayed({
                rv.animate().alpha(1f)
                    .setDuration(100)
                    .start()
                scroller.targetPosition = digits
                rv.layoutManager?.startSmoothScroll(scroller)
            }, delay)
            delay += 100
        }
        currentAmount = amount
    }

    private val Int.dp: Int
        get() = this * (context.resources.displayMetrics.density).toInt()

    inner class CustomSmoothScroller(
        private val isUp: Boolean
    ) : LinearSmoothScroller(context) {

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return 1000f / displayMetrics.densityDpi // 기본값: 25f
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            val time = super.calculateTimeForScrolling(dx)
            return minOf(time, 2000) // 최대 2초로 제한
        }

        // 감속 시간 조정
        override fun calculateTimeForDeceleration(dx: Int): Int {
            return (calculateTimeForScrolling(dx) * 0.8f).toInt()
        }

        // 스크롤 방향 조정
        override fun getVerticalSnapPreference(): Int {
            return if (isUp) {
                SNAP_TO_START
            } else {
                SNAP_TO_END
            }
        }
    }

    private fun initDigitsTextView(sidePadding: Int): TextView {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)

            setTextSize(TypedValue.COMPLEX_UNIT_DIP, amountTextSize)
            setTypeface(null, Typeface.BOLD)
            setPadding(sidePadding, 0, sidePadding, 0)
            includeFontPadding = false
        }
    }

    inner class DigitsAdapter : RecyclerView.Adapter<DigitsAdapter.DigitsViewHolder>() {

        private val dataList = (0..9).toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DigitsViewHolder {
            return DigitsViewHolder(initDigitsTextView(amountTextSideSpan))
        }

        override fun onBindViewHolder(holder: DigitsViewHolder, pos: Int) {
            holder.tv.text = dataList[pos].toString()
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class DigitsViewHolder(
            val tv: TextView
        ) : RecyclerView.ViewHolder(tv)
    }

    private class SingleItemRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : RecyclerView(context, attrs, defStyleAttr) {

        private var itemHeight = 0

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            // 아이템 높이 계산
            if (itemHeight == 0 && adapter != null && adapter!!.itemCount > 0) {
                val layoutManager = layoutManager
                if (layoutManager != null) {
                    val viewHolder = adapter!!.createViewHolder(this, 0)
                    adapter!!.onBindViewHolder(viewHolder, 0)

                    // 아이템 뷰 측정
                    val itemView = viewHolder.itemView
                    itemView.measure(
                        MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    )
                    itemHeight = itemView.measuredHeight
                }
            }

            // 높이를 아이템 하나의 높이로 제한
            if (itemHeight > 0) {
                val newHeight = itemHeight + paddingTop + paddingBottom
                setMeasuredDimension(measuredWidth, newHeight)
            }
        }
    }
}
