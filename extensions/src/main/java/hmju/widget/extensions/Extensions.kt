package hmju.widget.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Extensions {

    /**
     * Convert Dp to Int
     * ex. 5.dp
     */
    val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Convert Sp to Int
     * ex. 5.dp
     */
    val Int.sp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Convert Dp to Float
     * ex. 5F.sp
     */
    val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    /**
     * Convert Sp to Float
     * ex. 5F.sp
     */
    val Float.sp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )

    val Float.toSize: Float
        get() = this / Resources.getSystem().displayMetrics.density
    val Int.toSize: Int
        get() = this / Resources.getSystem().displayMetrics.density.toInt()

    /**
     * Get ActionBar Height
     */
    internal fun Context.actionBarHeight(): Float {
        val attrs: TypedArray = theme.obtainStyledAttributes(
            intArrayOf(R.attr.actionBarSize)
        )

        val actionBarSize: Float = attrs.getDimension(0, 0f)
        attrs.recycle()
        return actionBarSize
    }

    fun Context.getDeviceWidth(): Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun Context.getDeviceHeight(): Int {
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
     * StatusBar Height
     */
    fun Context.getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * NavigationBar Height
     */
    fun Context.getNavigationBarHeight(): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * is NavigationBar Height
     */
    fun Context.isNavigationBar(): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    /**
     * get Real ContentsHeight
     */
    fun Context.getRealContentsHeight(): Int {
        return getDeviceHeight()
            .minus(getStatusBarHeight())
            .minus(getNavigationBarHeight())
    }

    /**
     * 외 / 내부 여러 변수들의 NullCheck 를 하고자 무분별한 ?.let or ?.run 남용을 막기위해
     * 만든 함수 3개의 변수를 체크하는 함수
     * @return let 확장함수와 동일하게 고차함수 중간에 리턴 형태를 변경할수 있다.
     */
    inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
        return if (a != null && b != null) {
            function(a, b)
        } else {
            null
        }
    }

    /**
     * 외 / 내부 여러 변수들의 NullCheck 를 하고자 무분별한 ?.let or ?.run 남용을 막기위해
     * 만든 함수 3개의 변수를 체크하는 함수
     * @return let 확장함수와 동일하게 고차함수 중간에 리턴 형태를 변경할수 있다.
     */
    inline fun <A, B, C, R> multiNullCheck(a: A?, b: B?, c: C?, function: (A, B, C) -> R): R? {
        return if (a != null && b != null && c != null) {
            function(a, b, c)
        } else {
            null
        }
    }

    /**
     * 외 / 내부 여러 변수들의 NullCheck 를 하고자 무분별한 ?.let or ?.run 남용을 막기위해
     * 만든 함수 3개의 변수를 체크하는 함수
     * @return let 확장함수와 동일하게 고차함수 중간에 리턴 형태를 변경할수 있다.
     */
    inline fun <A, B, C, D, R> multiNullCheck(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        function: (A, B, C, D) -> R
    ): R? {
        return if (a != null && b != null && c != null && d != null) {
            function(a, b, c, d)
        } else {
            null
        }
    }

    inline fun <A, B> multiCompareLoop(aList: List<A>, bList: List<B>, function: (A, B) -> Unit) {
        val size = aList.size.coerceAtMost(bList.size)
        for (index in 0 until size) {
            function.invoke(aList[index], bList[index])
        }
    }

    /**
     * 뷰 캡처 처리하는 함수
     * @param callback Bitmap Callback Func.
     *
     */
    @Deprecated(
        message = "비동기 처리 방식은 사용자에게 맡기도록 처리 합니다. ",
        replaceWith = ReplaceWith("captureBitmap()")
    )
    fun View.captureBitmap(callback: (Bitmap) -> Unit) {
//        GlobalScope.launch {
//            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            withContext(Dispatchers.Default) { draw(Canvas(bitmap)) }
//            withContext(Dispatchers.Main) { callback(bitmap) }
//        }
    }

    /**
     * 해당 레이아웃 캡처 처리 함수
     * @return Bitmap
     */
    fun View.captureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap
    }

    /**
     * FlexibleImageView Background Capture Bitmap
     * @param srcBitmap FlexibleImageView 에서 사용한 Bitmap
     * @param srcRectF FlexibleImageView 에서 사용한 이미지 좌표 값 {@link FlexibleImageView.getStateItem()}
     * @param width FlexibleImageView 를 감싸는 레이아웃의 너비
     * @param height FlexibleImageView 를 감싸는 레이아웃의 높이
     * @param color 빈공간에 대한 색상 값
     *
     */
    fun backgroundCaptureBitmap(
        srcBitmap: Bitmap,
        srcRectF: RectF,
        width: Int,
        height: Int,
        @ColorInt color: Int = Color.WHITE
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempBitmap = Bitmap.createScaledBitmap(
            srcBitmap,
            srcRectF.width().toInt(),
            srcRectF.height().toInt(),
            true
        )
        Canvas(bitmap).apply {
            drawColor(color)
            drawBitmap(tempBitmap, null, srcRectF, null)
        }
        return bitmap
    }

    /**
     * isFakeDragging Check Current Item 처리 함수
     */
//    fun ViewPager2.currentItem(pos: Int, smoothScroll: Boolean = true) {
//        if (isFakeDragging) {
//            endFakeDrag()
//        }
//        setCurrentItem(pos, smoothScroll)
//    }

    /**
     * CustomView initBinding Function..
     * Invalid LayoutId or Not LifecycleOwner Extension
     * T -> Not Support Class
     * @param layoutId View Layout Id
     * @param lifecycleOwner View LifecycleOwner
     * @param isAdd 이 함수 내에서 View 를 추가 할건지? Default true,
     * @param apply 고차 함수. (Optional)
     */
//    inline fun <reified T : ViewDataBinding> ViewGroup.initBinding(
//        @LayoutRes layoutId: Int,
//        lifecycleOwner: LifecycleOwner,
//        isAdd: Boolean = true,
//        apply: T.() -> Unit = {}
//    ): T {
//        val viewRoot = LayoutInflater.from(context).inflate(layoutId, this, false)
//        val binding: T = DataBindingUtil.bind(viewRoot)
//            ?: throw NullPointerException("Invalid LayoutId")
//        binding.lifecycleOwner = lifecycleOwner
//
//        if (isAdd) {
//            addView(binding.root)
//        }
//
//        binding.apply(apply)
//
//        return binding
//    }

    /**
     * FragmentActivity 가져오는 View 기반 확장 함수
     * @return FragmentActivity Nullable
     */
    fun View.getFragmentActivity(): FragmentActivity? {
        if (context is FragmentActivity) {
            return context as FragmentActivity
        } else if (context is ContextWrapper) {
            var tmpContext = this.context
            while (tmpContext is ContextWrapper) {
                if (tmpContext is FragmentActivity) {
                    return tmpContext
                }
                tmpContext = tmpContext.baseContext
            }
        }
        return null
    }
}
