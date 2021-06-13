package hmju.widget.behavior

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import hmju.widget.BuildConfig
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

    /**
     * 0~1.0 의 맞게 ReDraw View Override Func
     * @param child : Changed View
     * @param percent : 0 ~ 1.0
     * @author hmju
     */
    abstract fun onRedraw(child: V, percent: Float)

    protected val TAG: String = "CustomBehavior"

    protected val mContext: Context = context
    protected val UNSPECIFIED_FLOAT: Float = Float.MAX_VALUE
    protected val UNSPECIFIED_INT: Int = Int.MAX_VALUE

    //[s]=====================AttributeSet Variable=====================//
    enum class TYPE {
        HORIZONTAL, VERTICAL
    }

    private var mDependId: Int = 0                  // Dependency View Id
    private var mDependPin: Float = 0F              // Dependency View Pin Height or Width
    private var mDependRange: Float = 0F            // Dependency View 스크롤 범위.
    private var mDependType: TYPE = TYPE.VERTICAL   // Behavior Type
    //[e]=====================AttributeSet Variable=====================//

    private var mSoundOnCreate: Boolean = true
    private var mDependX: Float = 0F                // Dependency Location X
    private var mDependY: Float = 0F                // Dependency Location Y
    protected var mDependWidth: Int = 0             // Dependency View Width
    protected var mDependHeight: Int = 0            // Dependency View Height

    init {
        val attr: TypedArray =
            mContext.obtainStyledAttributes(attrs, R.styleable.TranslationBehavior)
        mDependId = attr.getResourceId(R.styleable.TranslationBehavior_behavior_dependId, 0)
        mDependPin =
            attr.getDimension(R.styleable.TranslationBehavior_behavior_dependPin, UNSPECIFIED_FLOAT)

        // 해당 속성값 셋팅 하였는지 체크.
        if (mDependId == 0) {
            // Dependency Id 값을 셋팅 안안하는 경우 NullPointerException Throw
            throw NullPointerException("behavior_dependId is a required attribute.")
        }

        mDependType = TYPE.values()[attr.getInt(
            R.styleable.TranslationBehavior_behavior_dependType,
            1
        )] // 기본값은 Vertical
        mDependRange = attr.getDimension(
            R.styleable.TranslationBehavior_behavior_dependRange,
            UNSPECIFIED_FLOAT
        )  // Dependency Scroll Range
        attr.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        // 속성에서 ID 값을 지정 한 View 에 대해 true 반환.
        return dependency.id == mDependId
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
        if (mSoundOnCreate) {
            onCreate(parent, child, dependency)
        }

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
        mDependX = dependency.x
        mDependY = dependency.y
        mDependWidth = dependency.width
        mDependHeight = dependency.height

        // Dependency View Scroll Type Vertical 인 경우.
        if (TYPE.VERTICAL == mDependType) {
            // Dependency View 의 Pint 값을 속성에서 셋팅 안한경우 기본 ActionBarSize 로 셋팅.
            if (mDependPin == UNSPECIFIED_FLOAT) {
                mDependPin = mContext.actionBarHeight()
            }

            // 스크롤 범위 최대값 -> Dependency View 높이 - 상태바 높이
            val maxRange: Float = mDependHeight - mDependPin + mContext.statusBarHeight()
            // 스크롤 범위 값 제한.
            if (maxRange < mDependRange) {
                mDependRange = maxRange
            }
        } else {
            // TODO 나중에 Dependency 가 Horizontal 인경우 대응 해야함.
        }

        mSoundOnCreate = false
    }

    /**
     * 각 타입의 맞게 계산해서 Percentage 값을 {onRedraw(child: V, percent: Float)}
     * 로 보내는 함수.
     * @param child Target View
     * @param dependency Dependency View
     * @author hmju
     */
    private fun onUpdateCal(child: V, dependency: View) {

        val percent: Float
        val start: Float
        val current: Float
        val end: Float

        // 각 타입에 맞게 계산.
        when (mDependType) {
            TYPE.HORIZONTAL -> {
                start = mDependX
                current = dependency.x
                end = mDependRange
            }
            TYPE.VERTICAL -> {
                start = mDependY
                current = dependency.y
                end = mDependRange
            }
        }

        // Percentage Calculate.. 0.0 ~ 1.0
        percent = abs(current - start) / abs(end - start)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onUpdateCal\t${percent}")
        }

        // onReDraw
        onRedraw(child, if (percent > 1F) 1F else percent)
    }
}