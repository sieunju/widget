package hmju.widget.tablayout

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.R
import hmju.widget.extensions.dp

/**
 * Description : BaseTabLayout
 *
 * Created by juhongmin on 2021/12/31
 */
abstract class BaseTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), LifecycleOwner, LifecycleEventObserver {

    interface Listener {
        fun onTabClick(pos: Int, view: View)
    }

    abstract fun onPageSelect(pos: Int)
    abstract fun onPageScroll(pos: Int, offset: Float)
    abstract fun onPageScrollStated(@ViewPager2.ScrollState state: Int)

    enum class Type {
        FIXED, SCROLLABLE
    }

    /**
     * ViewPager2 PagerListener
     */
    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageSelect(position)
        }

        override fun onPageScrolled(
            pos: Int,
            offset: Float,
            offsetPixel: Int
        ) {
            // Offset 튀는 현상 방지
            if (offset < 1F) {
                onPageScroll(pos, offset)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollStated(state)
        }
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    var viewPager: ViewPager2? = null
        set(value) {
            if (value != null) {
                value.unregisterOnPageChangeCallback(pageListener)
                value.registerOnPageChangeCallback(pageListener)
            }

            field = value
        }

    // [s] Attribute Set Variable
    protected var type: Type = Type.FIXED
    protected var textSize = 16.dp

    @ColorInt
    protected var enableTxtColor = Color.BLACK

    @ColorInt
    protected var disableTxtColor = Color.BLACK

    @Dimension
    protected val bottomLineHeight: Float

    protected var isChangeTextStyle: Boolean = true // 선택된 Tab Text Style Bold 로 할건지 Flag 값
    protected val bottomLinePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }
    // [e] Attribute Set Variable

    init {
        isFillViewport = true
        isHorizontalScrollBarEnabled = false

        context.obtainStyledAttributes(attrs, R.styleable.BaseTabLayout).run {
            type = Type.values()[getInt(R.styleable.BaseTabLayout_tabType, 0)]
            // 룩핀 탭 기본 스타일로 셋팅
            textSize = getDimensionPixelSize(R.styleable.BaseTabLayout_tabTextSize, 16.dp)
            isChangeTextStyle = getBoolean(R.styleable.BaseTabLayout_tabIsChangeTextStyle, true)
            enableTxtColor = getColor(
                R.styleable.BaseTabLayout_tabTextColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
            disableTxtColor =
                getColor(
                    R.styleable.BaseTabLayout_tabDisableTextColor,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
            bottomLineHeight =
                getDimension(R.styleable.BaseTabLayout_tabBottomLineHeight, 0F)
            bottomLinePaint.color = getColor(R.styleable.BaseTabLayout_tabBottomLineColor, NO_ID)

            recycle()
        }
    }

    open fun onCreate() {}
    open fun onResume() {}
    open fun onStop() {}
    open fun onDestroy() {}

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("JLogger", "onStateChanged $event")
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {}
        }
    }

    override fun getLifecycle() = lifecycleRegistry

    /**
     * AddObserver
     * @param activity 자동으로 추가해주는 Observer 에서 Activity 찾지 못하는 경우
     * 수동으로 처리하는 함수 like.. Hilt 를 사용하는 경우 기본 getFragmentActivity 확장함수에서는
     * 찾지 못함.
     */
    fun addObserver(activity: FragmentActivity) {
        activity.lifecycle.addObserver(this)
    }
}