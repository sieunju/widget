package hmju.widget.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import hmju.widget.R
import hmju.widget.extensions.actionBarHeight
import hmju.widget.extensions.statusBarHeight
import kotlin.math.abs

/**
 * Description : BasePercentageBehavior
 * Description: CoordinatorLayout Behavior
 * 기반의 원하는 위치로 스크롤 했을경우 움직이도록 하는 기본 추상화 클래스.
 * Created by juhongmin on 6/13/21
 */
abstract class BasePercentageBehavior<V : View> @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout.Behavior<V>(context, attrs) {

    companion object {
        const val UNSPECIFIED_FLOAT: Float = Float.MAX_VALUE
        const val UNSPECIFIED_INT: Int = Int.MAX_VALUE
    }

    /**
     * 0~1.0 의 맞게 ReDraw View Override Func
     * @param child : Changed View
     * @param percent : 0 ~ 1.0
     * @author hmju
     */
    abstract fun onRedraw(child: V, percent: Float)

    private val ctx = context

    //[s]=====================AttributeSet Variable=====================//
    enum class Type {
        HORIZONTAL, VERTICAL
    }

    private var dependId: Int = 0                  // Dependency View Id
    private var dependPin: Float = 0F              // Dependency View Pin Height or Width
    private var dependRange: Float = 0F            // Dependency View 스크롤 범위.
    private var dependType: Type = Type.VERTICAL   // Behavior Type
    //[e]=====================AttributeSet Variable=====================//

    private var soundOnCreate: Boolean = true
    private var dependX: Float = 0F                // Dependency Location X
    private var dependY: Float = 0F                // Dependency Location Y
    protected var dependWidth: Int = 0             // Dependency View Width
    protected var dependHeight: Int = 0            // Dependency View Height

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TranslationBehavior).run {
            try {
                dependId = getResourceId(R.styleable.TranslationBehavior_behaviorDependId, 0)
                dependPin = getDimension(
                    R.styleable.TranslationBehavior_behaviorDependPin,
                    UNSPECIFIED_FLOAT
                )
                // 해당 속성값 셋팅 하였는지 체크.
                if (dependId == 0) {
                    // Dependency Id 값을 셋팅 안안하는 경우 NullPointerException Throw
                    throw NullPointerException("behaviorDependId is a required attribute.")
                }
                dependType =
                    Type.values()[getInt(R.styleable.TranslationBehavior_behaviorDependType, 1)]
                dependRange = getDimension(
                    R.styleable.TranslationBehavior_behaviorDependRange,
                    UNSPECIFIED_FLOAT
                )
            } catch (_: Exception) {
            }
            recycle()
        }
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        // 속성에서 ID 값을 지정 한 View 에 대해 true 반환.
        return dependency.id == dependId
    }

    /**
     *
     * Dependency View 가 변할때마다 해당 함수가 호출된다.
     */
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        // 최초 한번만 실행.
        if (soundOnCreate) {
            onCreate(parent, child, dependency)
        }

        TranslationBehavior.LogD("onDependentViewChanged $child")
        // percentage Calculate
        onUpdateCal(child, dependency)
        return false
    }

    /**
     * Behavior 최초 실행후 각 뷰들의 좌표값, 너비값을 셋팅하기 위한 함수.
     * @param parent Parent View -> CoordinatorLayout
     * @param child V
     * @param dependency View -> depend_id
     * @author hmju
     */
    open fun onCreate(parent: CoordinatorLayout, child: V, dependency: View) {
        dependX = dependency.x
        dependY = dependency.y
        dependWidth = dependency.width
        dependHeight = dependency.height

        // Dependency View Scroll Type Vertical 인 경우.
        if (Type.VERTICAL == dependType) {
            // Dependency View 의 Pint 값을 속성에서 셋팅 안한경우 기본 ActionBarSize 로 셋팅.
            if (dependPin == UNSPECIFIED_FLOAT) {
                dependPin = ctx.actionBarHeight()
            }

            // 스크롤 범위 최대값 -> Dependency View 높이 - 상태바 높이
            val maxRange: Float = dependHeight - dependPin + ctx.statusBarHeight()
            // 스크롤 범위 값 제한.
            if (maxRange < dependRange) {
                dependRange = maxRange
            }
        } else {
            // TODO 나중에 Dependency 가 Horizontal 인경우 대응 해야함.
        }

        soundOnCreate = false
    }

    /**
     * 각 타입의 맞게 계산해서 Percentage 값을 {onRedraw(child: V, percent: Float)}
     * 로 보내는 함수.
     * @param child Target View
     * @param dependency Dependency View
     * @author hmju
     */
    private fun onUpdateCal(child: V, dependency: View) {

        var percent: Float
        val start: Float
        val current: Float
        val end: Float

        // 각 타입에 맞게 계산.
        when (dependType) {
            Type.HORIZONTAL -> {
                start = dependX
                current = dependency.x
                end = dependRange
            }
            Type.VERTICAL -> {
                start = dependY
                current = dependency.y
                end = dependRange
            }
        }
        // Percentage Calculate.. 0.0 ~ 1.0
        percent = abs(current - start) / abs(end - start)
        if (percent.isNaN()) {
            TranslationBehavior.LogD("is NAN")
            percent = 0.0F
        }

        TranslationBehavior.LogD("onUpdateCal\t${percent}")

        // onReDraw
        onRedraw(child, if (percent > 1F) 1F else percent)
    }
}