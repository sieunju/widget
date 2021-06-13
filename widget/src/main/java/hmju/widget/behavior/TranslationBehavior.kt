package hmju.widget.behavior

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import hmju.widget.BuildConfig
import hmju.widget.R
import hmju.widget.extensions.statusBarHeight

/**
 * kotlinStudy
 * Class: TranslationBehavior
 * Created by jsieu on 2019-08-14.
 *
 * Description: BasePercentageBehavior 기반의
 * require - > Translation
 * optional -> Scale,Alpha
 * 액션을 취하는 Behavior Class
 */
class TranslationBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasePercentageBehavior<View>(context, attrs, defStyleAttr) {

    private var mStartX: Float = 0F                     // onCreate 시 X 좌표
    private var mStartY: Float = 0F                     // onCreate 시 Y 좌표.
    private var mEndX: Float = UNSPECIFIED_FLOAT        // 이동 하고 싶은 X 좌표
    private var mEndY: Float = UNSPECIFIED_FLOAT        // 이동 하고 싶은 Y 좌표

    private var mStartWidth: Int = 0                    // onCreate 시 Child View 너비
    private var mStartHeight: Int = 0                   // onCreate 시 Child View 높이
    private var mEndWidth: Int = 0                      // 원하는 Child View 너비
    private var mEndHeight: Int = 0                     // 원하는 Child View 높이

    private var mEndXType: TYPE =
        TYPE.START            // 이동하고 싶은 X 좌표의 기준 (Dependency View 의 X 위치값 기준.)
    private var mEndYType: TYPE =
        TYPE.START            // 이동하고 싶은 Y 좌표의 기준 (Dependency View 의 Y 위치값 기준.)
    private var mStartAlpha: Float = 0F
    private val mEndAlpha: Float

    internal enum class TYPE(val value: String) {
        START("s"),
        END("e")
    }

    init {

        val attr: TypedArray =
            mContext.obtainStyledAttributes(attrs, R.styleable.TranslationBehavior)

        val endX: String? = attr.getString(R.styleable.TranslationBehavior_behavior_endX)
        // endY Null 값이 아닌경우 mEndY 에 알맞게 값 셋팅 한다.
        if (endX != null) {
            val splitStr: List<String> = endX.split(",")
            when (splitStr.size) {
                // ChildY Format 형태를 암묵적으로 표시한 경우.
                1 -> mEndX = castStringToDp(mContext, splitStr[0])
                // ChildY Format 형태를 명시적으로 표시한 경우.
                2 -> {
                    // 끝 기준인경우 Type 값 변경.
                    if (TYPE.END.value == splitStr[0]) {
                        mEndXType = TYPE.END
                    }
                    mEndX = castStringToDp(mContext, splitStr[1])
                }
                else -> throw IllegalArgumentException("behavior_childX does not match the format type. ex.) s,73 or 73")
            }
        }

        val endY: String? = attr.getString(R.styleable.TranslationBehavior_behavior_endY)
        // endY Null 값이 아닌경우 mEndY 에 알맞게 값 셋팅 한다.
        if (endY != null) {
            val splitStr: List<String> = endY.split(",")
            when (splitStr.size) {
                // ChildY Format 형태를 암묵적으로 표시한 경우.
                1 -> mEndY = castStringToDp(
                    mContext,
                    splitStr[0]
                ) + mContext.statusBarHeight()
                // ChildY Format 형태를 명시적으로 표시한 경우.
                2 -> {
                    // 끝 기준인경우 Type 값 변경.
                    if (TYPE.END.value == splitStr[0]) {
                        mEndYType = TYPE.END
                    }
                    mEndY = castStringToDp(
                        mContext,
                        splitStr[1]
                    ) + mContext.statusBarHeight()
                }
                else -> throw IllegalArgumentException("behavior_childX does not match the format type. ex.) s,73 or 73")
            }
        }

        mEndWidth = attr.getDimensionPixelOffset(
            R.styleable.TranslationBehavior_behavior_endWidth,
            UNSPECIFIED_INT
        )
        mEndHeight = attr.getDimensionPixelOffset(
            R.styleable.TranslationBehavior_behavior_endHeight,
            UNSPECIFIED_INT
        )

        mEndAlpha =
            attr.getFloat(R.styleable.TranslationBehavior_behavior_endAlpha, UNSPECIFIED_FLOAT)
        attr.recycle()
    }

    /**
     * View onCreate
     * @param parent : CoordinatorLayout
     * @param child : View
     * @param dependency : Dependency View
     *
     * @author hmju
     */
    override fun onCreate(parent: CoordinatorLayout, child: View, dependency: View) {
        super.onCreate(parent, child, dependency)

        // Start Setting.
        mStartX = child.x
        mStartY = child.y
        mStartWidth = child.width
        mStartHeight = child.height
        mStartAlpha = child.alpha

        // Dependency View 기준으로 끝 지점에서 좌표값 계산하는 경우.
        if (mEndXType == TYPE.END) {
            mEndX = mDependWidth - (mEndX + mStartWidth)

            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (mEndWidth != UNSPECIFIED_INT && mStartWidth > mEndWidth) {
                mEndX += (mStartWidth - mEndWidth) / 2
            }
            // 점점 커지는 경우.
            else if (mEndWidth != UNSPECIFIED_INT && mStartWidth < mEndWidth) {
                mEndX -= (mEndWidth - mStartWidth) / 2
            }
        }
        // Dependency View 기준으로 시작 지점에서 좌표값 계산하는 경우.
        else {
            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (mEndWidth != UNSPECIFIED_INT && mStartWidth > mEndWidth) {
                mEndX -= (mStartWidth - mEndWidth) / 2
            }
            // 점점 커지는 경우.
            else if (mEndWidth != UNSPECIFIED_INT && mStartWidth < mEndWidth) {
                mEndX += (mEndWidth - mStartWidth) / 2
            }
        }

        // Dependency View 기준으로 끝에서 좌표값 계산하는 경우.
        if (mEndYType == TYPE.END) {
            mEndY = mDependHeight - (mEndY + mStartHeight)

            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (mEndHeight != UNSPECIFIED_INT && mStartHeight > mEndHeight) {
                mEndY += (mStartHeight - mEndWidth) / 2
            }
            // 점점 커지는 경우.
            else if (mEndHeight != UNSPECIFIED_INT && mStartHeight < mEndHeight) {
                mEndY -= (mEndHeight - mStartHeight) / 2
            }
        }
        // Dependency View 기준으로 시작 지점에서 좌표값 계산하는 경우.
        else {
            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (mEndHeight != UNSPECIFIED_INT && mStartHeight > mEndHeight) {
                mEndY -= (mStartHeight - mEndWidth) / 2
            }
            // 점점 커지는 경우.
            else if (mEndHeight != UNSPECIFIED_INT && mStartHeight < mEndHeight) {
                mEndY += (mEndHeight - mStartHeight) / 2
            }
        }
    }

    /**
     * Dependency View 가 Scroll 비율에 따라서 View 를 다시 그리는 함수.
     * @param child Child View
     * @param percent 스크롤 범위의 비율 0.0 ~ 1.0
     * @author hmju
     */
    override fun onRedraw(child: View, percent: Float) {

        // set Location.
        var newX: Float = if (mEndX == UNSPECIFIED_FLOAT) 0F else (mEndX - mStartX) * percent
        var newY: Float = if (mEndY == UNSPECIFIED_FLOAT) 0F else (mEndY - mStartY) * percent

        // 속성값중에 끝나는 너비 값을 지정한 경우.
        if (mEndWidth != UNSPECIFIED_INT) {
            val newWidth: Float = mStartWidth + ((mEndWidth - mStartWidth) * percent)
            child.scaleX = newWidth / mStartWidth
        }

        // 속성값중에 끝나는 높이 값을 지정한 경우.
        if (mEndHeight != UNSPECIFIED_INT) {
            val newHeight: Float = mStartHeight + ((mEndHeight - mStartHeight) * percent)
            child.scaleY = newHeight / mStartHeight
        }

        child.translationX = newX
        child.translationY = newY

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Child Location\t${child.x}\t${child.y}")
        }

        // 속성값중 알파값을 지정한 경우.
        if (mEndAlpha != UNSPECIFIED_FLOAT) {
            // 비율 계산.
            child.alpha = mStartAlpha + (mEndAlpha - mStartAlpha) * percent
        }

        child.requestLayout()
    }

    /**
     * String -> Dp Cast Func
     * @param ctx Context
     * @param dp 변환 하고 싶은 Dpi 숫자.
     * @author hmju
     */
    private fun castStringToDp(@NonNull ctx: Context, dp: String): Float {
        dp.replace("dp", "")
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            ctx.resources.displayMetrics
        )
    }
}

