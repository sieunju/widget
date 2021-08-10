package hmju.widget.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import hmju.widget.R

/**
 * Description :
 *
 * Created by juhongmin on 8/9/21
 */
class CustomLayout @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CustomLayout"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private var enableDrawable: GradientDrawable? = null
    private var disableDrawable: GradientDrawable? = null
    private var isClicked: Boolean = true

    private var corner: Float = 0F

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomLayout).run {
            try {
                val defState = getBoolean(R.styleable.CustomLayout_layoutDefState, true)
                corner = getDimension(R.styleable.CustomLayout_layoutCorner, -1F)

                var color = getColor(R.styleable.CustomLayout_layoutBg_color, Color.WHITE)
                var strokeWidth =
                    getDimensionPixelSize(R.styleable.CustomLayout_layoutBorder, NO_ID)
                var strokeColor = getColor(R.styleable.CustomLayout_layoutBorderColor, NO_ID)

                enableDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(color, color)
                ).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }

                color = getColor(R.styleable.CustomLayout_layoutDisableBgColor, Color.WHITE)
                strokeWidth =
                    getDimensionPixelSize(R.styleable.CustomLayout_layoutDisableBorder, NO_ID)
                strokeColor = getColor(R.styleable.CustomLayout_layoutDisableBorderColor, NO_ID)

                disableDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(color, color)
                ).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }

                // Default Setting
                background = if (defState) {
                    enableDrawable
                } else {
                    disableDrawable
                }

            } catch (_: Exception) {
            }
            recycle()
        }

        clipToOutline = true
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        background = if (selected) {
            enableDrawable
        } else {
            disableDrawable
        }
        super.setSelected(selected)
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        background = if (enabled) {
            enableDrawable
        } else {
            disableDrawable
        }
        super.setEnabled(enabled)
    }

    /**
     * setEnable Drawable Code Type
     *
     * @param color Bg Color
     * @param strokeWidth Border Size
     * @param strokeColor Border Color
     *
     */
    fun setEnableDrawable(
        @ColorInt color: Int,
        corner: Float,
        strokeWidth: Int,
        @ColorInt strokeColor: Int
    ) {
        if (enableDrawable != null) {
            enableDrawable = null
        }
        enableDrawable =
            GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
                if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                    setStroke(strokeWidth, strokeColor)
                }
            }
    }

    /**
     * setDisable Drawable Code Type
     *
     * @param color Bg Color
     * @param strokeWidth Border Size
     * @param strokeColor Border Color
     *
     */
    fun setDisableDrawable(
        @ColorInt color: Int,
        corner: Float,
        strokeWidth: Int,
        @ColorInt strokeColor: Int
    ) {
        if (disableDrawable != null) {
            disableDrawable = null
        }
        disableDrawable =
            GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
                if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                    setStroke(strokeWidth, strokeColor)
                }
            }
    }

}