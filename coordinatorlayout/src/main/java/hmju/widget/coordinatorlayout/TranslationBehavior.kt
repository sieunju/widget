package hmju.widget.coordinatorlayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlin.math.roundToInt

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
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TranslationBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BasePercentageBehavior<View>(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TranslationBehavior"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private var startX: Float = 0F                     // onCreate 시 X 좌표
    private var startY: Float = 0F                     // onCreate 시 Y 좌표.
    private var endX: Float = UNSPECIFIED_FLOAT        // 이동 하고 싶은 X 좌표
    private var endY: Float = UNSPECIFIED_FLOAT        // 이동 하고 싶은 Y 좌표

    private var startWidth: Int = 0                    // onCreate 시 Child View 너비
    private var startHeight: Int = 0                   // onCreate 시 Child View 높이
    private var endWidth: Int = 0                      // 원하는 Child View 너비
    private var endHeight: Int = 0                     // 원하는 Child View 높이

    private var endXType: Type =
        Type.START            // 이동하고 싶은 X 좌표의 기준 (Dependency View 의 X 위치값 기준.)
    private var endYType: Type =
        Type.START            // 이동하고 싶은 Y 좌표의 기준 (Dependency View 의 Y 위치값 기준.)
    private var startAlpha: Float = 0F
    private var endAlpha: Float = 0.0f

    internal enum class Type(val value: String) {
        START("s"),
        END("e")
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TranslationBehavior).run {
            try {
                val isFitsSystemWindow =
                    getBoolean(R.styleable.TranslationBehavior_behaviorIsFitsSystemWindow, false)
                getString(R.styleable.TranslationBehavior_behaviorEndX)?.let {
                    val split = it.split(",")
                    // $(s or e -> Optional},${Location X -> Required}
                    when (split.size) {
                        1 -> endX = split[0].strToDp(context)
                        2 -> {
                            if (Type.END.value == split[0]) {
                                endXType = Type.END
                            }
                            endX = split[1].strToDp(context)
                        }
                        else -> throw IllegalArgumentException("behaviorEndX does not match the format type. ex.) s,73 or 73")
                    }
                }

                getString(R.styleable.TranslationBehavior_behaviorEndY)?.let {
                    val split = it.split(",")
                    // $(s or e -> Optional},${Location Y -> Required}
                    when (split.size) {
                        1 -> endY = split[0].strToDp(context)
                            .plus(if (isFitsSystemWindow) context.getStatusBarHeight() else 0)
                        2 -> {
                            // 끝 기준인경우 Type 값 변경.
                            if (Type.END.value == split[0]) {
                                endYType = Type.END
                            }
                            endY = split[1].strToDp(context)
                                .plus(if (isFitsSystemWindow) context.getStatusBarHeight() else 0)
                        }
                        else -> throw IllegalArgumentException("behaviorEndY does not match the format type. ex.) s,73 or 73")
                    }
                }

                endWidth = getDimensionPixelOffset(
                    R.styleable.TranslationBehavior_behaviorEndWidth,
                    UNSPECIFIED_INT
                )
                endHeight = getDimensionPixelOffset(
                    R.styleable.TranslationBehavior_behaviorEndHeight,
                    UNSPECIFIED_INT
                )
                endAlpha =
                    getFloat(R.styleable.TranslationBehavior_behaviorEndAlpha, UNSPECIFIED_FLOAT)

            } catch (_: Exception) {
            }
            recycle()
        }
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
        startX = child.x
        startY = child.y
        startWidth = child.width
        startHeight = child.height
        startAlpha = child.alpha

        // Dependency View 기준으로 끝 지점에서 좌표값 계산하는 경우.
        if (endXType == Type.END) {
            endX = dependWidth - (endX + startWidth)

            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (endWidth != UNSPECIFIED_INT && startWidth > endWidth) {
                endX += (startWidth - endWidth) / 2
            } else if (endWidth != UNSPECIFIED_INT && startWidth < endWidth) {
                // 점점 커지는 경우.
                endX -= (endWidth - startWidth) / 2
            }
        }
        // Dependency View 기준으로 시작 지점에서 좌표값 계산하는 경우.
        else {
            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (endWidth != UNSPECIFIED_INT && startWidth > endWidth) {
                endX -= (startWidth - endWidth) / 2
            } else if (endWidth != UNSPECIFIED_INT && startWidth < endWidth) {
                // 점점 커지는 경우.
                endX += (endWidth - startWidth) / 2
            }
        }

        // Dependency View 기준으로 끝에서 좌표값 계산하는 경우.
        if (endYType == Type.END) {
            endY = dependHeight - (endY + startHeight)

            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (endHeight != UNSPECIFIED_INT && startHeight > endHeight) {
                endY += (startHeight - endWidth) / 2
            } else if (endHeight != UNSPECIFIED_INT && startHeight < endHeight) {
                // 점점 커지는 경우.
                endY -= (endHeight - startHeight) / 2
            }
        } else {
            // Dependency View 기준으로 시작 지점에서 좌표값 계산하는 경우.
            // EndWidth 가 지정 되어 있는 경우 EndWidth 값에 맞게 좌표값 계산.
            if (endHeight != UNSPECIFIED_INT && startHeight > endHeight) {
                endY -= (startHeight - endWidth) / 2
            } else if (endHeight != UNSPECIFIED_INT && startHeight < endHeight) {
                // 점점 커지는 경우.
                endY += (endHeight - startHeight) / 2
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
        val newX: Float = if (endX == UNSPECIFIED_FLOAT) 0F else (endX - startX) * percent
        val newY: Float = if (endY == UNSPECIFIED_FLOAT) 0F else (endY - startY) * percent

        // 속성값중에 끝나는 너비 값을 지정한 경우.
        if (endWidth != UNSPECIFIED_INT) {
            val newWidth: Float = startWidth + ((endWidth - startWidth) * percent)
            child.scaleX = newWidth / startWidth
        }

        // 속성값중에 끝나는 높이 값을 지정한 경우.
        if (endHeight != UNSPECIFIED_INT) {
            val newHeight: Float = startHeight + ((endHeight - startHeight) * percent)
            child.scaleY = newHeight / startHeight
        }

        child.translationX = (newX.roundToInt()).toFloat()
        child.translationY = (newY.roundToInt()).toFloat()

        LogD("Child Location\t${child.x}\t${child.y}")

        // 속성값중 알파값을 지정한 경우.
        if (endAlpha != UNSPECIFIED_FLOAT) {
            // 비율 계산.
            child.alpha = startAlpha + (endAlpha - startAlpha) * percent
        }

        child.requestLayout()
    }

    /**
     * String to Dp
     */
    private fun String.strToDp(ctx: Context): Float {
        return try {
            val str = this.replace("dp", "")
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                str.toFloat(),
                ctx.resources.displayMetrics
            )
        } catch (_: Exception) {
            0F
        }
    }
}
