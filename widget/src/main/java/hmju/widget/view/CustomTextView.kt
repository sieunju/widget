package hmju.widget.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatTextView
import hmju.widget.R
import hmju.widget.extensions.toSize

/**
 * Description : AppCompatTextView 기반의
 * Corner, Border, BgColor 등등 drawable 사용하지 않고 코드로
 * 처리 할수 있는 TextView Class
 *
 * Created by hmju on 2021-08-09
 */
class CustomTextView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(ctx, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CustomTextView"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    data class Item(
            @ColorInt var txtColor: Int = Color.BLACK,
            var drawable: GradientDrawable? = null
    ) {
        var corner: Float = -1F
            set(value) {
                if (value != -1F) {
                    drawable?.cornerRadius = value
                }
                field = value
            }

        fun setStroke(@Dimension width: Int, @ColorInt color: Int) {
            if (width != -1 && color != NO_ID) {
                drawable?.setStroke(width, color)
            }
        }
    }

    private val enableItem = Item()
    private val disableItem = Item()
    private var isClicked: Boolean = true

    // [s] 자동 크기 조절
    private var isAutoSizing: Boolean = false
    private var autoMaxSize: Float = -1F
    private var autoMinSize: Float = -1F
//    private var spacingAdd: Float = 0.0F
//    private var spacingMult: Float = 1.0F
    // [e] 자동 크기 조절

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomTextView).run {
            try {
                if (getBoolean(R.styleable.CustomTextView_textViewIsAuto, true)) {
                    val corner = getDimension(R.styleable.CustomTextView_textViewCorner, -1F)
                    val defState = getBoolean(R.styleable.CustomTextView_textViewDefState, true)
                    var txtColor =
                            getColor(R.styleable.CustomTextView_textViewTxtColor, Color.BLACK)
                    var bgColor =
                            getColor(R.styleable.CustomTextView_textViewBgColor, Color.WHITE)
                    var strokeWidth =
                            getDimensionPixelSize(R.styleable.CustomTextView_textViewBorder, -1)
                    var strokeColor =
                            getColor(R.styleable.CustomTextView_textViewBorderColor, NO_ID)

                    enableItem.apply {
                        this.txtColor = txtColor
                        this.drawable = GradientDrawable(
                                GradientDrawable.Orientation.BL_TR,
                                intArrayOf(bgColor, bgColor)
                        )
                        this.corner = corner
                        this.setStroke(strokeWidth, strokeColor)
                    }

                    txtColor =
                            getColor(R.styleable.CustomTextView_textViewDisableTxtColor, Color.BLACK)
                    bgColor =
                            getColor(R.styleable.CustomTextView_textViewDisableBgColor, Color.WHITE)
                    strokeWidth =
                            getDimensionPixelSize(R.styleable.CustomTextView_textViewDisableBorder, -1)
                    strokeColor =
                            getColor(R.styleable.CustomTextView_textViewDisableBorderColor, NO_ID)

                    disableItem.apply {
                        this.txtColor = txtColor
                        this.drawable = GradientDrawable(
                                GradientDrawable.Orientation.BL_TR,
                                intArrayOf(bgColor, bgColor)
                        )
                        this.corner = corner
                        this.setStroke(strokeWidth, strokeColor)
                    }

                    //Default State
                    background = if (defState) {
                        setTextColor(enableItem.txtColor)
                        enableItem.drawable
                    } else {
                        setTextColor(disableItem.txtColor)
                        disableItem.drawable
                    }

                    // Auto Size
                    autoMaxSize =
                            getDimension(R.styleable.CustomTextView_textViewAutoMaxSize, -1F)
                    autoMinSize =
                            getDimension(R.styleable.CustomTextView_textViewAutoMinSize, -1F)
                    isAutoSizing = autoMaxSize != -1F && autoMinSize != -1F


                    if (isAutoSizing) {
                        // MaxLines 가 정해져 있지 않으면 1로 치환
                        if (maxLines == Int.MAX_VALUE) maxLines = 1
                        textSize = autoMaxSize.toSize
//                        this@CustomTextView.text = text
                    }
                }
            } catch (_: Exception) {
            }
            recycle()
        }
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        if (selected) {
            background = enableItem.drawable
            setTextColor(enableItem.txtColor)
        } else {
            background = disableItem.drawable
            setTextColor(disableItem.txtColor)
        }
        super.setSelected(selected)
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        if (enabled) {
            background = enableItem.drawable
            setTextColor(enableItem.txtColor)
        } else {
            background = disableItem.drawable
            setTextColor(disableItem.txtColor)
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
        enableItem.drawable = null
        enableItem.drawable =
                GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }
        performUpdate()
    }

    /**
     * setEnable Text Color Code Type
     * @param color TextColor
     */
    fun setEnableTxtColor(@ColorInt color: Int) {
        enableItem.txtColor = color
        performUpdate()
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
        disableItem.drawable = null
        disableItem.drawable =
                GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(color, color)).apply {
                    if (corner != -1F) {
                        cornerRadius = corner
                    }
                    if (strokeWidth != NO_ID && strokeColor != NO_ID) {
                        setStroke(strokeWidth, strokeColor)
                    }
                }
        performUpdate()
    }

    /**
     * setDisable Text Color Code Type
     * @param color TextColor
     */
    fun setDisableTxtColor(@ColorInt color: Int) {
        disableItem.txtColor = color
        performUpdate()
    }

    private fun performUpdate() {
        if (disableItem.drawable != null && enableItem.drawable != null) {
            background = if (isClicked) {
                enableItem.drawable
            } else {
                disableItem.drawable
            }
        }

        setTextColor(if (isClicked) enableItem.txtColor else disableItem.txtColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isAutoSizing) {
            post {
                try {
                    val maxSize = autoMaxSize.toInt().toSize
                    for (size in maxSize downTo 1) {
                        val txtLayout = textLayout(text)
                        LogD("onMeasure $size  ${txtLayout.lineCount}")
                        if(size == 1 && txtLayout.lineCount > maxLines) {
                            isAutoSizing = false
                            return@post
                        }
                        if(txtLayout.lineCount > maxLines) {
                            textSize = size.toFloat()
                        } else {
                            LogD("나갑니다~")
                            break
                        }
                    }
                } catch (ex : Exception) {
                    LogD("Error $ex")
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun textLayout(text: CharSequence): Layout {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StaticLayout.Builder.obtain(
                    text,
                    0,
                    text.length,
                    paint,
                    width - (paddingLeft + paddingRight)
            ).apply {
                setAlignment(Layout.Alignment.ALIGN_NORMAL)
                setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                setIncludePad(false)
                setMaxLines(maxLines)
            }.build()
        } else {
            return StaticLayout(
                    text, paint,
                    width - (paddingLeft + paddingRight),
                    Layout.Alignment.ALIGN_NORMAL,
                    lineSpacingMultiplier,
                    lineSpacingExtra, false
            )
        }
    }

}
