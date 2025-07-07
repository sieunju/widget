package hmju.widget.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

/**
 * Description :
 *
 * Created by juhongmin on 2025. 7. 7.
 */
class RollingAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var currentAmount = 0
    private var targetAmount = 0
    private val customAdapter = AmountAdapter()
    private val snapHelper = LinearSnapHelper()
    private var isAnimating = false

    // 애니메이션 설정
    private val animationDuration = 3000L
    private val maxJumpRange = Int.MAX_VALUE // 너무 멀면 시작점 조정

    init {
        setupRecyclerView()
        setupGradientEffect()
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = customAdapter
        snapHelper.attachToRecyclerView(this)
        isNestedScrollingEnabled = false

        post {
            scrollToPosition(customAdapter.itemCount / 2)
        }
    }

    private fun setupGradientEffect() {
        // 그라데이션을 onDraw에서 직접 그리는 방식으로 변경
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGradientOverlay(canvas)
    }

    private fun drawGradientOverlay(canvas: Canvas) {
        val gradientHeight = dpToPx(60)

        // 상단 그라데이션
        val topGradient = LinearGradient(
            0f, 0f, 0f, gradientHeight.toFloat(),
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        val topPaint = Paint().apply { shader = topGradient }
        canvas.drawRect(0f, 0f, width.toFloat(), gradientHeight.toFloat(), topPaint)

        // 하단 그라데이션
        val bottomGradient = LinearGradient(
            0f, height - gradientHeight.toFloat(), 0f, height.toFloat(),
            Color.TRANSPARENT, Color.WHITE,
            Shader.TileMode.CLAMP
        )
        val bottomPaint = Paint().apply { shader = bottomGradient }
        canvas.drawRect(
            0f,
            height - gradientHeight.toFloat(),
            width.toFloat(),
            height.toFloat(),
            bottomPaint
        )
    }

    fun setAmount(amount: Int) {
        if (amount == targetAmount) return

        targetAmount = amount

        // 시작점 조정 로직
        val distance = Math.abs(amount - currentAmount)
        if (distance > maxJumpRange) {
            // 너무 멀면 적절한 시작점으로 조정
            currentAmount = if (amount > currentAmount) {
                amount - maxJumpRange / 2
            } else {
                amount + maxJumpRange / 2
            }
        }

        animateToAmount(amount)
    }

    private fun animateToAmount(targetAmount: Int) {
        isAnimating = true

        val startAmount = currentAmount
        val animator = ValueAnimator.ofInt(startAmount, targetAmount).apply {
            duration = animationDuration
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                val animatedAmount = animation.animatedValue as Int
                customAdapter.setCurrentAmount(animatedAmount)

                // 현재 아이템 위치로 스크롤
                val targetPosition = customAdapter.getPositionForAmount(animatedAmount)
                // smoothScrollToPosition(targetPosition)
                scrollToPosition(targetPosition)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    currentAmount = targetAmount
                }
            })
        }

        animator.start()
    }

    private fun updateCurrentAmount() {
        val layoutManager = layoutManager as LinearLayoutManager
        val centerPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (centerPosition != NO_POSITION) {
            currentAmount = customAdapter.getAmountAtPosition(centerPosition)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // 커스텀 어댑터
    private inner class AmountAdapter : Adapter<AmountViewHolder>() {

        private var currentDisplayAmount = 0
        var totalItems = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmountViewHolder {
            val textView = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(80) // 아이템 높이
                )
                gravity = Gravity.CENTER
                textSize = 28f
                setTextColor(Color.BLACK)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(16, 0, 16, 0)
            }
            return AmountViewHolder(textView)
        }

        override fun onBindViewHolder(holder: AmountViewHolder, position: Int) {
            val amount = getAmountAtPosition(position)
            val formattedAmount = formatAmount(amount)
            holder.textView.text = formattedAmount

            // 중앙 아이템 강조 효과 (옵션)
            holder.textView.alpha = 1.0f
        }

        override fun getItemCount(): Int {
            if (totalItems == -1) {
                return 0
            } else {
                return totalItems
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setCurrentAmount(amount: Int) {
            currentDisplayAmount = amount
            totalItems = 5
            notifyDataSetChanged()
        }

        fun getAmountAtPosition(position: Int): Int {
            val centerPosition = totalItems / 2
            val offset = position - centerPosition
            return maxOf(0, currentDisplayAmount + offset * 1000) // 1000원 단위
        }

        fun getPositionForAmount(amount: Int): Int {
            val centerPosition = totalItems / 2
            val offset = ((amount - currentDisplayAmount) / 1000)
            return (centerPosition + offset).coerceIn(0, totalItems - 1)
        }

        private fun formatAmount(amount: Int): String {
            return "₩${NumberFormat.getNumberInstance().format(amount)}"
        }
    }

    // 뷰홀더
    private class AmountViewHolder(val textView: TextView) : ViewHolder(textView)
}