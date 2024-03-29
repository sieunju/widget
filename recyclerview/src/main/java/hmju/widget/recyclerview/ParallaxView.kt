package hmju.widget.recyclerview

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.ceil

/**
 * Description : RecyclerView 에서 디바이스 기준
 * 원하는 범위에서 크기가 줄어들었다 넓어졌다하는 View
 *
 * Created by juhongmin on 9/20/21
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ParallaxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ViewTreeObserver.OnScrollChangedListener {

    companion object {
        private const val TAG = "ParallaxView"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    interface Listener {
        /**
         * 아래 기준으로 View 위치에 따라서 0.0 ~ 0.9999
         * 전달하는 리스너
         * @param percent 0.0 ~ 0.9999
         */
        fun onPercent(@FloatRange(from = 0.0, to = 1.0) percent: Float)
    }

    private val deviceY: Int by lazy { context.getRealContentsHeight() }
    private var startPoint: Double = 0.0 // 디바이스 하단 기준 시작점
    private var endPoint: Double = 0.0 // 디바이스 하단 기준 종료점
    private var viewHeight: Int = 0
    private var calculation: Int = 0
    private var parallaxMaxHeight: Int = 0
    private var parallaxMinHeight: Int = 0
    private var listener: Listener? = null

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
                    startPoint = deviceY - start.toDouble()
                    endPoint = end.toDouble()
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

            resizeHeight()
            requestLayout()
            calculation = parallaxMaxHeight - parallaxMinHeight
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    /**
     * 스크롤 위치에 따라 높이를 재조정 처리하는 함수.
     */
    private fun resizeHeight() {
        val current: Float = (bottom + top).toFloat() / 2F
        if (current in endPoint..startPoint) {
            var percent =
                (kotlin.math.abs(current - startPoint) / kotlin.math.abs(startPoint - endPoint)).toFloat()
            percent = percent.coerceAtMost(1.0F)
            LogD("Percent $percent")
            val resizeHeight = ceil((calculation * percent) + parallaxMinHeight).toInt()
            if (layoutParams.height != resizeHeight) {
                layoutParams = layoutParams.also { it.height = resizeHeight }
            }
            listener?.onPercent(percent)
        } else {
            LogD("Other Current $current")
            // 위에 있는 경우
            if (current < endPoint) {
                if (layoutParams.height != parallaxMaxHeight) {
                    layoutParams = layoutParams.also { it.height = parallaxMaxHeight }
                }
                listener?.onPercent(1.0F)
            } else {
                // 아래에 있는 경우
                if (layoutParams.height != parallaxMinHeight) {
                    layoutParams = layoutParams.also { it.height = parallaxMinHeight }
                }
                listener?.onPercent(0.0F)
            }
        }
    }

    /**
     * Callback method to be invoked when something in the view tree
     * has been scrolled.
     */
    override fun onScrollChanged() {
        resizeHeight()
    }

    private fun Context.getDeviceHeight(): Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    /**
     * Convert Dp to Int
     * ex. 5.dp
     */
    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * StatusBar Height
     */
    private fun Context.getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * NavigationBar Height
     */
    private fun Context.getNavigationBarHeight(): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * is NavigationBar Height
     */
    private fun Context.isNavigationBar(): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    /**
     * get Real ContentsHeight
     */
    private fun Context.getRealContentsHeight(): Int {
        return getDeviceHeight()
            .minus(getStatusBarHeight())
            .minus(getNavigationBarHeight())
    }
}
