package hmju.widget.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import hmju.widget.R
import hmju.widget.extensions.deviceHeight
import hmju.widget.extensions.dp
import java.lang.Math.abs

/**
 * Description : RecyclerView 에서 디바이스 기준
 * 원하는 범위에서 크기가 줄어들었다 넓어졌다하는 View
 *
 * Created by juhongmin on 9/20/21
 */
class ParallaxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ViewTreeObserver.OnScrollChangedListener {

    companion object {
        private const val TAG = "ParallaxView"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private val deviceY: Int by lazy { context.deviceHeight() }
    private var startPoint: Double = 0.0 // 디바이스 하단 기준 시작점
    private var endPoint: Double = 0.0 // 디바이스 하단 기준 종료점
    private var viewHeight: Int = 0
    private var calculation: Int = 0
    private var parallaxMaxHeight: Int = 0
    private var parallaxMinHeight: Int = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ParallaxView).run {
            try {
                var start = getDimensionPixelOffset(R.styleable.ParallaxView_parallaxStartDp, -1)
                var end = getDimensionPixelOffset(R.styleable.ParallaxView_parallaxEndDp, -1)

                // 고정값이 아닌경우 퍼센트인지 확인
                if (start == -1 || end == -1) {
                    start = getInteger(R.styleable.ParallaxView_parallaxStartPer, -1)
                    end = getInteger(R.styleable.ParallaxView_parallaxEndPer, -1)

                    if (start > 100 || end > 100) {
                        throw IllegalArgumentException("Int Range 0 ~ 100")
                    }

                    // 퍼센트도 아닌 경우 기본 기준인 40% ~ 80%
                    if (start == -1 || end == -1) {
                        startPoint = deviceY - (deviceY.toDouble() * (40F / 100F))
                        endPoint = (deviceY.toDouble() * (20F / 100F))
                    } else {
                        startPoint = deviceY - (deviceY.toDouble() * (start.toFloat() / 100F))
                        endPoint = (deviceY.toDouble() * (100F - end.toFloat()) / 100F)
                    }
                } else {
                    startPoint = start.toDouble()
                    endPoint = deviceY - end.toDouble()
                }

                parallaxMaxHeight =
                    getDimensionPixelSize(R.styleable.ParallaxView_parallaxMaxHeight, -1)
                parallaxMinHeight =
                    getDimensionPixelSize(R.styleable.ParallaxView_parallaxMinHeight, 50.dp)
            } catch (_: Exception) {

            }

            LogD("StartPoint $startPoint End $endPoint")
            LogD("DeviceHeight $deviceY")
            recycle()
        }

        post {
            if (parent is ViewGroup) {
                (parent as ViewGroup).viewTreeObserver.addOnScrollChangedListener(this)
            }

            this.viewHeight = height
            if (parallaxMaxHeight == -1) {
                parallaxMaxHeight = this.viewHeight
            }

            layoutParams = layoutParams.also { it.height = parallaxMinHeight }

            requestLayout()
            calculation = parallaxMaxHeight - parallaxMinHeight
            LogD("Calculation  ${calculation} Min ${parallaxMinHeight}  Max $parallaxMaxHeight")

        }
    }

    /**
     * Callback method to be invoked when something in the view tree
     * has been scrolled.
     */
    override fun onScrollChanged() {
        val current: Double = (bottom + top).toDouble() / 2F
        LogD("Scroll ${current}")
        if (current in endPoint..startPoint) {
            val percent: Double = abs(current - startPoint) / abs(startPoint - endPoint)
            val resizeHeight = Math.ceil((calculation * percent) + parallaxMinHeight).toInt()
            LogD("Percent $percent")
            if(layoutParams.height != resizeHeight) {
                layoutParams = layoutParams.also { it.height = resizeHeight }
            }
        } else {
            if(current < endPoint) {
                if(layoutParams.height != parallaxMaxHeight) {
                    layoutParams = layoutParams.also { it.height = parallaxMaxHeight }
                }
            } else {
                if(layoutParams.height != parallaxMinHeight) {
                    layoutParams = layoutParams.also { it.height = parallaxMinHeight }
                }
            }
        }
    }
}