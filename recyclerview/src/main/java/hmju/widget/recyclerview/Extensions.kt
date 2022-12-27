package hmju.widget.recyclerview

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

@Suppress("unused", "MemberVisibilityCanBePrivate")
internal object Extensions {

    /**
     * Convert Dp to Int
     * ex. 5.dp
     */
    internal val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Convert Sp to Int
     * ex. 5.dp
     */
    internal val Int.sp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Convert Dp to Float
     * ex. 5F.sp
     */
    internal val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    /**
     * Convert Sp to Float
     * ex. 5F.sp
     */
    internal val Float.sp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )

    internal val Float.toSize: Float
        get() = this / Resources.getSystem().displayMetrics.density
    internal val Int.toSize: Int
        get() = this / Resources.getSystem().displayMetrics.density.toInt()

    internal fun Context.getDeviceWidth(): Int {
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

    internal fun Context.getDeviceHeight(): Int {
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
    internal fun Context.getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * NavigationBar Height
     */
    internal fun Context.getNavigationBarHeight(): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }

    /**
     * is NavigationBar Height
     */
    internal fun Context.isNavigationBar(): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    /**
     * get Real ContentsHeight
     */
    internal fun Context.getRealContentsHeight(): Int {
        return getDeviceHeight()
            .minus(getStatusBarHeight())
            .minus(getNavigationBarHeight())
    }
}
