package hmju.widget.view

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.hmju.view.R

/**
 * Description : Image Border, Corner ImageView 함수
 * Image Border 처리로 인한 SDK Min Version 23
 * Created by juhongmin on 2022/12/30
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.M)
open class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var enableFgDrawable: GradientDrawable? = null // Corner, Border
    private var enableBgDrawable: GradientDrawable? = null // Corner
    private var disableFgDrawable: GradientDrawable? = null // Corner, Border
    private var disableBgDrawable: GradientDrawable? = null // Corner
    private var isClicked: Boolean = true

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomImageView).run {
            val defState = getBoolean(R.styleable.CustomImageView_imgViewDefState, true)

            // Enable Setting
            backgroundSetting(this)
            foregroundSetting(this)
            recycle()

            setBackground(defState)
            setForeground(defState)
        }
        clipToOutline = true
    }

    override fun setBackground(background: Drawable?) {
        // super.setBackground(background)
    }

    override fun setForeground(foreground: Drawable?) {
        // super.setForeground(foreground)
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        setBackground(selected)
        setForeground(selected)
        super.setSelected(selected)
    }

    /**
     * SetCorner
     * @param corner TargetCorner
     */
    fun setCorner(corner: Float) {
        if (isClicked) {
            handleCorner(corner, enableBgDrawable)
            handleCorner(corner, enableFgDrawable)
        } else {
            handleCorner(corner, disableBgDrawable)
            handleCorner(corner, disableBgDrawable)
        }
    }

    private fun setBackground(isSelected: Boolean) {
        super.setBackground(
            if (isSelected) {
                enableBgDrawable
            } else {
                disableBgDrawable
            }
        )
    }

    private fun setForeground(isSelected: Boolean) {
        super.setForeground(
            if (isSelected) {
                enableFgDrawable
            } else {
                disableFgDrawable
            }
        )
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        setBackground(enabled)
        setForeground(enabled)
        super.setEnabled(enabled)
    }

    /**
     * Background Setting
     */
    private fun backgroundSetting(attribute: TypedArray) {
        attribute.run {
            var corner = getDimension(R.styleable.CustomImageView_imgViewCorner, -1F)
            var color = getColor(R.styleable.CustomImageView_imgViewBgColor, Color.TRANSPARENT)
            enableBgDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(color, color)
            ).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
            }

            corner = getDimension(R.styleable.CustomImageView_imgViewDisableCorner, -1F)
            color = getColor(R.styleable.CustomImageView_imgViewDisableBgColor, Color.TRANSPARENT)

            disableBgDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(color, color)
            ).apply {
                if (corner != -1F) {
                    cornerRadius = corner
                }
            }
        }

    }

    /**
     * ForegroundSetting Setting
     */
    private fun foregroundSetting(attribute: TypedArray) {
        attribute.run {
            var corner = getDimension(R.styleable.CustomImageView_imgViewCorner, -1F)
            var border = getDimensionPixelSize(R.styleable.CustomImageView_imgViewBorder, -1)
            var borderColor =
                getColor(R.styleable.CustomImageView_imgViewBorderColor, Color.TRANSPARENT)
            enableFgDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)
            ).apply {
                if (corner > 0) {
                    cornerRadius = corner
                }
                if (border > 0) {
                    setStroke(border, borderColor)
                }
            }
            corner = getDimension(R.styleable.CustomImageView_imgViewDisableCorner, -1F)
            border = getDimensionPixelSize(R.styleable.CustomImageView_imgViewDisableBorder, -1)
            borderColor =
                getColor(R.styleable.CustomImageView_imgViewDisableBorderColor, Color.TRANSPARENT)
            disableFgDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)
            ).apply {
                if (corner > 0) {
                    cornerRadius = corner
                }
                if (border > 0) {
                    setStroke(border, borderColor)
                }
            }
        }
    }

    private fun handleCorner(corner: Float, drawable: GradientDrawable?) {
        if (drawable == null) return
        drawable.cornerRadius = corner
    }

}