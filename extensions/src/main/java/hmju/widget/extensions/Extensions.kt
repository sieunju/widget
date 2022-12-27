package hmju.widget.extensions

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

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
}
