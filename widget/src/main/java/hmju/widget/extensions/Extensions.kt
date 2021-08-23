package hmju.widget.extensions

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue


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
 * Convert Dp to Float
 * ex. 5F.dp
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.toSize: Float
    get() = this / Resources.getSystem().displayMetrics.density
val Int.toSize: Int
    get() = this / Resources.getSystem().displayMetrics.density.toInt()


/**
 * Get Status Bar Height
 */
fun Context.statusBarHeight(): Int {
    val id = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (id > 0) resources.getDimensionPixelSize(id) else -1
}

/**
 * Get ActionBar Height
 */
fun Context.actionBarHeight(): Float {
    val attrs: TypedArray = theme.obtainStyledAttributes(
        intArrayOf(android.R.attr.actionBarSize)
    )

    val actionBarSize: Float = attrs.getDimension(0, 0f)
    attrs.recycle()
    return actionBarSize
}

fun Context.deviceWidth(): Int {
    return resources.displayMetrics.widthPixels
}

fun Context.deviceHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun Context.isNavigationBar(): Boolean {
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && resources.getBoolean(id)
}

// MultiPle Null Check.
inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
    return if (a != null && b != null) {
        function(a, b)
    } else {
        null
    }
}

// MultiPle Null Check.
inline fun <A, B, C, R> multiNullCheck(a: A?, b: B?, c: C?, function: (A, B, C) -> R): R? {
    return if (a != null && b != null && c != null) {
        function(a, b, c)
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