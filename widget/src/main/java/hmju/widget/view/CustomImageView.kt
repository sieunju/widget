package hmju.widget.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView

/**
 * Description : Glide 에 맞는 ImageView
 *
 * Created by juhongmin on 10/24/21
 */
class CustomImageView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(ctx, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CustomImageView"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    init {

    }
}