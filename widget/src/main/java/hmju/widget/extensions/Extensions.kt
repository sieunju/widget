package hmju.widget.extensions

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    GlobalScope.launch {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        withContext(Dispatchers.Default) { draw(Canvas(bitmap)) }
        withContext(Dispatchers.Main) { callback(bitmap) }
    }
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