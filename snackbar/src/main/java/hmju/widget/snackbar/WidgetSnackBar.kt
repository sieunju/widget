package hmju.widget.snackbar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

/**
 * Description : WidgetSnackBar Class
 * Context 에서 Activity 를 찾아 Snackbar 를 표시하는 유틸리티 클래스
 *
 * Created by juhongmin on 2026. 2. 4.
 */
class WidgetSnackBar private constructor(
    private val activity: Activity
) {

    private var message: CharSequence = ""
    private var duration: Int = Snackbar.LENGTH_SHORT
    private var anchorView: View? = null
    private var actionText: CharSequence? = null
    private var actionListener: View.OnClickListener? = null

    /**
     * Snackbar 메시지 설정
     * @param message 표시할 메시지
     */
    fun message(message: CharSequence): WidgetSnackBar {
        this.message = message
        return this
    }

    /**
     * Snackbar 메시지 설정 (String Resource)
     * @param resId 표시할 메시지 리소스 ID
     */
    fun message(@StringRes resId: Int): WidgetSnackBar {
        this.message = activity.getString(resId)
        return this
    }

    /**
     * Snackbar 표시 시간 설정
     * @param duration Snackbar.LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE
     */
    fun duration(duration: Int): WidgetSnackBar {
        this.duration = duration
        return this
    }

    /**
     * Snackbar Anchor View 설정
     * @param view Snackbar 가 표시될 기준 View
     */
    fun anchorView(view: View): WidgetSnackBar {
        this.anchorView = view
        return this
    }

    /**
     * Snackbar Action 버튼 설정
     * @param text Action 버튼 텍스트
     * @param listener Action 버튼 클릭 리스너
     */
    fun action(text: CharSequence, listener: View.OnClickListener): WidgetSnackBar {
        this.actionText = text
        this.actionListener = listener
        return this
    }

    /**
     * Snackbar 표시
     */
    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        val rootView = activity.findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, duration)

        anchorView?.let { snackbar.setAnchorView(it) }

        val actionText = this.actionText
        val actionListener = this.actionListener
        if (actionText != null && actionListener != null) {
            snackbar.setAction(actionText, actionListener)
        }

        snackbar.show()
    }

    companion object {

        /**
         * Context 에서 Activity 를 찾는 함수
         * ContextWrapper 를 재귀적으로 탐색하여 Activity 를 찾는다.
         *
         * @param context UI Context (Activity, ContextWrapper 등)
         * @return Activity or null
         */
        @JvmStatic
        fun findActivity(context: Context): Activity? {
            var currentContext = context
            while (currentContext is ContextWrapper) {
                if (currentContext is Activity) {
                    return currentContext
                }
                currentContext = currentContext.baseContext
            }
            return null
        }

        /**
         * WidgetSnackBar 인스턴스 생성
         * @param context UI Context (Activity 를 포함하는 Context)
         * @return WidgetSnackBar 인스턴스
         * @throws IllegalStateException Activity 를 찾을 수 없는 경우
         */
        @JvmStatic
        fun with(context: Context): WidgetSnackBar {
            val activity = findActivity(context)
                ?: throw IllegalStateException(
                    "WidgetSnackBar 는 Activity 기반의 Context 가 필요합니다. " +
                            "Application Context 에서는 사용할 수 없습니다."
                )
            return WidgetSnackBar(activity)
        }

        /**
         * 간편하게 Snackbar 를 표시하는 함수
         * @param context UI Context
         * @param message 표시할 메시지
         * @param duration Snackbar.LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE
         */
        @JvmStatic
        fun show(
            context: Context,
            message: CharSequence,
            duration: Int = Snackbar.LENGTH_SHORT
        ) {
            with(context)
                .message(message)
                .duration(duration)
                .show()
        }

        /**
         * 간편하게 Snackbar 를 표시하는 함수 (String Resource)
         * @param context UI Context
         * @param resId 표시할 메시지 리소스 ID
         * @param duration Snackbar.LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE
         */
        @JvmStatic
        fun show(
            context: Context,
            @StringRes resId: Int,
            duration: Int = Snackbar.LENGTH_SHORT
        ) {
            with(context)
                .message(resId)
                .duration(duration)
                .show()
        }
    }
}
