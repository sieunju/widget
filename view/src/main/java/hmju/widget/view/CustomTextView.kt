package hmju.widget.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat

/**
 * Description : Corner, Border, AutoTextSize 비/활성화 상태에 따라서도
 * 처리 해주는 AppCompatTextView 기반의 View Class
 *
 * Created by hmju on 2021-08-09
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomTextView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(ctx, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CustomTextView"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    data class Item(
        @ColorInt var txtColor: Int? = null,
        var drawable: GradientDrawable? = null,
        @StyleRes var textStyle: Int = NO_ID
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
    private var isClicked: Boolean = true // 활성화, 비활성화

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
                var corner = getDimension(R.styleable.CustomTextView_textViewCorner, -1F)
                isClicked = getBoolean(R.styleable.CustomTextView_textViewDefState, true)
                var txtColor: Int? = null
                txtColor = if (hasValue(R.styleable.CustomTextView_textViewTxtColor)) {
                    getColor(R.styleable.CustomTextView_textViewTxtColor, Color.BLACK)
                } else {
                    null
                }
                var bgColor = getColor(R.styleable.CustomTextView_textViewBgColor, Color.WHITE)
                var strokeWidth =
                    getDimensionPixelSize(R.styleable.CustomTextView_textViewBorder, -1)
                var strokeColor =
                    getColor(R.styleable.CustomTextView_textViewBorderColor, NO_ID)
                var textStyle = getResourceId(R.styleable.CustomTextView_textViewTextStyle, NO_ID)

                // TextStyle 를 우선순위로
                if (textStyle != NO_ID) {
                    txtColor = null
                }

                enableItem.apply {
                    this.txtColor = txtColor
                    this.drawable = GradientDrawable(
                        GradientDrawable.Orientation.BL_TR,
                        intArrayOf(bgColor, bgColor)
                    )
                    this.corner = corner
                    this.setStroke(strokeWidth, strokeColor)
                    this.textStyle = textStyle
                }

                txtColor = if (hasValue(R.styleable.CustomTextView_textViewDisableTxtColor)) {
                    getColor(R.styleable.CustomTextView_textViewDisableTxtColor, Color.BLACK)
                } else {
                    null
                }

                bgColor =
                    getColor(R.styleable.CustomTextView_textViewDisableBgColor, Color.WHITE)
                strokeWidth =
                    getDimensionPixelSize(R.styleable.CustomTextView_textViewDisableBorder, -1)
                strokeColor =
                    getColor(R.styleable.CustomTextView_textViewDisableBorderColor, NO_ID)
                textStyle =
                    getResourceId(R.styleable.CustomTextView_textViewDisableTextStyle, NO_ID)
                corner = getDimension(R.styleable.CustomTextView_textViewDisableCorner, -1F)

                // TextStyle 를 우선순위로
                if (textStyle != NO_ID) {
                    txtColor = null
                }

                disableItem.apply {
                    this.txtColor = txtColor
                    this.drawable = GradientDrawable(
                        GradientDrawable.Orientation.BL_TR,
                        intArrayOf(bgColor, bgColor)
                    )
                    this.corner = corner
                    this.setStroke(strokeWidth, strokeColor)
                    this.textStyle = textStyle
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
            } catch (_: Exception) {
            }
            recycle()
        }

        // Default State
        performInvalidate()
    }

    private fun setCustomBackground(isEnable: Boolean) {
        if (isEnable) {
            if (enableItem.drawable != null) {
                background = enableItem.drawable
            }
        } else {
            if (disableItem.drawable != null) {
                background = disableItem.drawable
            }
        }
    }

    /**
     * set TextStyle
     * @param isEnable 활 / 비활성화
     */
    private fun setCustomTextStyle(isEnable: Boolean) {
        if (isEnable) {
            // 텍스트 스타일이 정해져 있는 경우 -> TextColor 무시
            if (enableItem.textStyle != NO_ID) {
                TextViewCompat.setTextAppearance(this, enableItem.textStyle)
            } else if (enableItem.txtColor != null) {
                setTextColor(enableItem.txtColor!!)
            } else {
                // 기본값 블랙
                setTextColor(Color.BLACK)
            }
        } else {
            // 텍스트 스타일이 정해져 있는 경우 -> TextColor 무시
            if (disableItem.textStyle != NO_ID) {
                TextViewCompat.setTextAppearance(this, disableItem.textStyle)
            } else if (disableItem.txtColor != null) {
                setTextColor(disableItem.txtColor!!)
            } else {
                // 기본값 블랙
                setTextColor(Color.BLACK)
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        isClicked = selected
        performInvalidate()
        super.setSelected(selected)
    }

    override fun setEnabled(enabled: Boolean) {
        isClicked = enabled
        performInvalidate()
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
        performInvalidate()
    }

    /**
     * setEnable Text Color Code Type
     * @param color TextColor
     */
    fun setEnableTxtColor(@ColorInt color: Int) {
        enableItem.txtColor = color
        performInvalidate()
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
        performInvalidate()
    }

    /**
     * setDisable Text Color Code Type
     * @param color TextColor
     */
    fun setDisableTxtColor(@ColorInt color: Int) {
        disableItem.txtColor = color
        performInvalidate()
    }

    /**
     * 텍스트 Background And TextStyle UI Update
     */
    private fun performInvalidate() {
        setCustomBackground(isClicked)
        setCustomTextStyle(isClicked)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        if (isAutoSizing) {
//            post {
//                try {
//                    val maxSize = autoMaxSize.toInt().toSize
//                    for (size in maxSize downTo 1) {
//                        val txtLayout = textLayout(text)
//                        LogD("onMeasure $size  ${txtLayout.lineCount}")
//                        if(size == 1 && txtLayout.lineCount > maxLines) {
//                            isAutoSizing = false
//                            return@post
//                        }
//                        if(txtLayout.lineCount > maxLines) {
//                            textSize = size.toFloat()
//                        } else {
//                            LogD("나갑니다~")
//                            break
//                        }
//                    }
//                } catch (ex : Exception) {
//                    LogD("Error $ex")
//                }
//            }
//        }
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

    val Float.toSize: Float
        get() = this / Resources.getSystem().displayMetrics.density
}
