package hmju.widget.tablayout

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.viewpager2.widget.ViewPager2

/**
 * Description : BaseTabLayout
 *
 * Created by juhongmin on 2021/12/31
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class BaseTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), LifecycleOwner, LifecycleEventObserver {

    interface Listener {
        fun onTabClick(pos: Int, view: View)
    }

    interface TabClickListener {
        fun onTabClick(pos: Int)
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

    @StyleRes
    protected var enableStyle: Int = NO_ID

    @StyleRes
    protected var disableStyle: Int = NO_ID

    @Dimension
    protected val bottomLineHeight: Float

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

            if (hasValue(R.styleable.BaseTabLayout_tabEnableTextStyle) &&
                hasValue(R.styleable.BaseTabLayout_tabDisableTextStyle)
            ) {
                enableStyle = getResourceId(R.styleable.BaseTabLayout_tabEnableTextStyle, NO_ID)
                disableStyle = getResourceId(R.styleable.BaseTabLayout_tabDisableTextStyle, NO_ID)
            } else {
                // throw
                throw IllegalArgumentException("Require styleable is tabEnableTextStyle And tabDisableTextStyle")
            }

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
        lifecycleRegistry.handleLifecycleEvent(event)
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
     * @param fragment 자동으로 추가해주는 Observer 에서 Activity 찾지 못하는 경우
     * 수동으로 처리하는 함수 like.. Hilt 를 사용하는 경우 기본 getFragmentActivity 확장함수에서는
     * 찾지 못함.
     */
    fun addObserver(fragment: Fragment) {
        fragment.lifecycle.addObserver(this)
    }

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
