package hmju.widget.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import java.text.NumberFormat

/**
 * Description :
 *
 * Created by juhongmin on 2025. 7. 7.
 */
class FallingAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var currentAmount = 0
    private var targetAmount = 0
    private val digitViews = mutableListOf<SingleFallingDigitView>()
    private val separatorViews = mutableListOf<TextView>()
    private val currencyView = TextView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setupViews()
    }

    private fun setupViews() {
        // 통화 기호와 구분자도 같은 높이로 맞춤
        currencyView.apply {
            text = "₩"
            textSize = 48f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 16, 0)
            gravity = Gravity.CENTER // 세로 가운데 정렬
        }
        addView(currencyView)
    }

    fun setAmount(amount: Int) {
        if (amount == targetAmount) return

        targetAmount = amount
        setupDigitsForAmount(amount)

        // 약간의 딜레이 후 애니메이션 시작 (뷰가 완전히 그려진 후)
        post {
            startFallingAnimation()
        }
    }

    private fun setupDigitsForAmount(amount: Int) {
        // 기존 뷰들 제거
        removeAllViews()
        digitViews.clear()
        separatorViews.clear()

        // 통화 기호 다시 추가
        addView(currencyView)

        // 금액을 문자열로 변환하여 자릿수 분석
        val amountStr = NumberFormat.getNumberInstance().format(amount)

        // 각 문자에 대해 뷰 생성
        for (i in amountStr.indices) {
            val char = amountStr[i]

            if (char.isDigit()) {
                // 숫자인 경우 떨어지는 뷰 생성
                val digitView = SingleFallingDigitView(context)
                digitViews.add(digitView)
                addView(digitView)
            } else if (char == ',') {
                // 콤마인 경우 구분자 뷰 생성
                val separatorView = TextView(context).apply {
                    text = ","
                    textSize = 48f
                    setTextColor(Color.GRAY)
                    setPadding(4, 0, 4, 0)
                    gravity = Gravity.CENTER // 세로 가운데 정렬
                    typeface = Typeface.DEFAULT_BOLD // 같은 폰트 스타일 적용
                }
                separatorViews.add(separatorView)
                addView(separatorView)
            }
        }
    }

    private fun startFallingAnimation() {
        val amountStr = NumberFormat.getNumberInstance().format(targetAmount)
        val digits = mutableListOf<Int>()

        // 숫자만 추출
        for (char in amountStr) {
            if (char.isDigit()) {
                digits.add(char.toString().toInt())
            }
        }

        println("Starting animation for amount: $targetAmount")
        println("Amount string: $amountStr")
        println("Digits: $digits")
        println("Total digit views: ${digitViews.size}")

        // 각 자릿수를 순차적으로 애니메이션
        for (i in digits.indices) {
            if (i < digitViews.size) {
                val targetDigit = digits[i]
                val delay = i * 50L

                println("Scheduling digit $targetDigit at index $i with delay $delay")

                postDelayed({
                    println("Animating digit $targetDigit at index $i")
                    digitViews[i].animateToDigit(targetDigit)
                }, delay)
            }
        }
    }

    // 개별 떨어지는 숫자 뷰
    private class SingleFallingDigitView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private var currentDigit = -1 // -1을 빈 상태로 초기화
        private var targetDigit = 0
        private var animationProgress = 0f
        private var isAnimating = false

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 68f
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        private val animationDuration = 200L
        private var animator: ValueAnimator? = null

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        fun animateToDigit(digit: Int) {
            println("animateToDigit called with digit: $digit, current: $currentDigit, isAnimating: $isAnimating")

            targetDigit = digit
            // 초기값을 0이 아닌 빈 상태로 설정
            if (currentDigit == 0 && !isAnimating) {
                currentDigit = -1 // -1을 빈 상태로 사용
            }
            startAnimation()
        }

        private fun startAnimation() {
            animator?.cancel()
            isAnimating = true

            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animationDuration
                interpolator = FastOutSlowInInterpolator()

                addUpdateListener { animation ->
                    animationProgress = animation.animatedValue as Float
                    invalidate()
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimating = false
                        currentDigit = targetDigit
                        animationProgress = 0f
                        invalidate()
                    }
                })
            }

            animator?.start()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val centerX = width / 2f
            val centerY = height / 2f

            // 텍스트를 세로 가운데 정렬하기 위한 베이스라인 계산
            val textBounds = Rect()
            val sampleText = "0"
            paint.getTextBounds(sampleText, 0, sampleText.length, textBounds)
            val textCenterY = centerY + textBounds.height() / 2f

            if (!isAnimating) {
                // 애니메이션이 없을 때는 현재 숫자 표시 (단, -1이면 빈 상태)
                if (currentDigit >= 0) {
                    canvas.drawText(currentDigit.toString(), centerX, textCenterY, paint)
                }
            } else {
                // 떨어지는 애니메이션
                drawFallingDigit(canvas, centerX, textCenterY)
            }
        }

        private fun drawFallingDigit(canvas: Canvas, centerX: Float, textCenterY: Float) {
            // 떨어지는 위치 계산
            val startY = -paint.textSize * 3 // 더 높은 위치에서 시작
            val endY = textCenterY
            val currentY = startY + (endY - startY) * animationProgress

            // 투명도 계산
            val alpha = when {
                animationProgress < 0.2f -> animationProgress / 0.2f
                animationProgress > 0.8f -> 1f
                else -> 0.8f + (animationProgress - 0.2f) / 0.6f * 0.2f
            }.coerceIn(0f, 1f)

            paint.alpha = (255 * alpha).toInt()

            // 떨어지는 숫자 그리기
            canvas.drawText(targetDigit.toString(), centerX, currentY, paint)

            // 잔상 효과 (더 위쪽에 흐릿한 숫자)
            if (animationProgress > 0.3f && animationProgress < 0.9f) {
                val trailY = currentY - paint.textSize * 0.5f
                paint.alpha = (128 * (1f - animationProgress) * 0.5f).toInt()
                canvas.drawText(targetDigit.toString(), centerX, trailY, paint)
            }

            paint.alpha = 255
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = (paint.measureText("0") + 10).toInt()
            val height = (paint.textSize * 2).toInt()
            setMeasuredDimension(width, height)
        }
    }
}