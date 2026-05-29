package hmju.widget.snackbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes

/**
 * Description : Activity 레이어에서 Top/Bottom 위치 지정 및 연속 이벤트 처리를 지원하는 커스텀 SnackBar
 *
 * 연속 이벤트 처리:
 *  - Phase 1: 기존 SnackBar dismiss 애니메이션
 *  - Phase 2: 새로운 SnackBar show 애니메이션 (dismiss 완료 후 실행)
 *  - 연속 호출 시 최신 요청만 대기열에 유지 (중간 요청 드롭)
 *
 * Created by juhongmin
 */
class CustomSnackBar private constructor(
    private val activity: Activity
) {

    enum class Position { TOP, BOTTOM }

    private var message: CharSequence = ""
    private var duration: Long = 2500L
    private var position: Position = Position.BOTTOM

    fun message(message: CharSequence): CustomSnackBar {
        this.message = message
        return this
    }

    fun message(@StringRes resId: Int): CustomSnackBar {
        this.message = activity.getString(resId)
        return this
    }

    fun duration(durationMs: Long): CustomSnackBar {
        this.duration = durationMs
        return this
    }

    fun position(position: Position): CustomSnackBar {
        this.position = position
        return this
    }

    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return
        Controller.show(activity, message, position, duration)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // State Machine
    // ─────────────────────────────────────────────────────────────────────────

    private data class Request(
        val activity: Activity,
        val message: CharSequence,
        val position: Position,
        val duration: Long
    )

    private class State {
        var entry: SnackEntry? = null
        var isDismissing = false
        var pending: Request? = null
        var autoDismissRunnable: Runnable? = null
    }

    private object Controller {
        private val handler = Handler(Looper.getMainLooper())
        private val states = HashMap<Int, State>()

        fun show(activity: Activity, message: CharSequence, position: Position, duration: Long) {
            val key = System.identityHashCode(activity)
            val state = states.getOrPut(key) { State() }
            val request = Request(activity, message, position, duration)

            when {
                // 아무것도 표시 중이 아님 → 바로 표시
                state.entry == null -> doShow(key, state, request)
                // Dismiss 중 → 최신 요청으로 교체 (대기)
                state.isDismissing -> state.pending = request
                // 표시 중 → Phase1 dismiss 후 Phase2 show
                else -> {
                    state.pending = request
                    cancelAutoDismiss(state)
                    dismissEntry(key, state)
                }
            }
        }

        private fun doShow(key: Int, state: State, request: Request) {
            if (request.activity.isFinishing || request.activity.isDestroyed) {
                states.remove(key)
                return
            }
            val decorView = request.activity.window.decorView as ViewGroup
            val entry = SnackEntry.create(decorView, request.message, request.position)
            state.entry = entry
            state.isDismissing = false
            state.pending = null
            entry.showWithAnimation()

            val runnable = Runnable {
                val s = states[key] ?: return@Runnable
                if (s.entry === entry && !s.isDismissing) dismissEntry(key, s)
            }
            state.autoDismissRunnable = runnable
            handler.postDelayed(runnable, request.duration)
        }

        private fun dismissEntry(key: Int, state: State) {
            val entry = state.entry ?: return
            state.isDismissing = true
            entry.dismissWithAnimation {
                val s = states[key] ?: return@dismissWithAnimation
                s.entry = null
                s.isDismissing = false
                val pending = s.pending
                if (pending != null) doShow(key, s, pending) else states.remove(key)
            }
        }

        private fun cancelAutoDismiss(state: State) {
            state.autoDismissRunnable?.let { handler.removeCallbacks(it) }
            state.autoDismissRunnable = null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View
    // ─────────────────────────────────────────────────────────────────────────

    private class SnackEntry(
        val view: FrameLayout,
        val parent: ViewGroup,
        val position: Position
    ) {
        // show animation post가 실행되기 전에 dismiss가 요청된 경우를 처리
        private var dismissCalled = false

        companion object {
            private const val ANIM_MS = 220L
            private val SLIDE_IN = DecelerateInterpolator()
            private val SLIDE_OUT = AccelerateInterpolator()

            fun create(
                decorView: ViewGroup,
                message: CharSequence,
                position: Position
            ): SnackEntry {
                val ctx = decorView.context
                val dp = ctx.resources.displayMetrics.density

                val windowInsets = decorView.rootWindowInsets
                val topInset = windowInsets?.systemWindowInsetTop ?: 0
                val bottomInset = windowInsets?.systemWindowInsetBottom ?: 0

                val snackView = buildSnackView(ctx, dp, message)
                val params = buildLayoutParams(dp, position, topInset, bottomInset)
                decorView.addView(snackView, params)

                return SnackEntry(snackView, decorView, position)
            }

            private fun buildSnackView(
                ctx: Context,
                dp: Float,
                message: CharSequence
            ) = FrameLayout(ctx).apply {
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#323232"))
                    cornerRadius = 8f * dp
                }
                elevation = 8f * dp
                addView(
                    TextView(ctx).apply {
                        text = message
                        setTextColor(Color.WHITE)
                        textSize = 14f
                        val vPad = (12 * dp).toInt()
                        val hPad = (16 * dp).toInt()
                        setPadding(hPad, vPad, hPad, vPad)
                    },
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            private fun buildLayoutParams(
                dp: Float,
                position: Position,
                topInset: Int,
                bottomInset: Int
            ) = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val hMargin = (16 * dp).toInt()
                val vMargin = (12 * dp).toInt()
                marginStart = hMargin
                marginEnd = hMargin
                when (position) {
                    Position.BOTTOM -> {
                        gravity = Gravity.BOTTOM
                        bottomMargin = bottomInset + vMargin
                    }
                    Position.TOP -> {
                        gravity = Gravity.TOP
                        topMargin = topInset + vMargin
                    }
                }
            }
        }

        fun showWithAnimation() {
            view.post {
                if (dismissCalled) return@post
                val startY = slideOffset()
                view.translationY = startY
                view.alpha = 0f
                view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(ANIM_MS)
                    .setInterpolator(SLIDE_IN)
                    .setListener(null)
                    .start()
            }
        }

        fun dismissWithAnimation(onEnd: () -> Unit) {
            dismissCalled = true
            view.animate().cancel()
            // show post 가 아직 실행 전이라 view 가 보이지 않는 경우 바로 제거
            if (view.alpha == 0f && view.translationY == 0f) {
                parent.removeView(view)
                onEnd()
                return
            }
            view.animate()
                .translationY(slideOffset())
                .alpha(0f)
                .setDuration(ANIM_MS)
                .setInterpolator(SLIDE_OUT)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        parent.removeView(view)
                        onEnd()
                    }
                })
                .start()
        }

        // Position 에 따른 슬라이드 방향 오프셋
        private fun slideOffset(): Float {
            val h = view.height.takeIf { it > 0 } ?: 200
            return if (position == Position.BOTTOM) h.toFloat() else -h.toFloat()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {

        @JvmStatic
        fun with(context: Context): CustomSnackBar {
            val activity = findActivity(context)
                ?: throw IllegalStateException(
                    "CustomSnackBar 는 Activity 기반의 Context 가 필요합니다."
                )
            return CustomSnackBar(activity)
        }

        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            message: CharSequence,
            position: Position = Position.BOTTOM,
            duration: Long = 2500L
        ) {
            with(context).message(message).position(position).duration(duration).show()
        }

        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            @StringRes resId: Int,
            position: Position = Position.BOTTOM,
            duration: Long = 2500L
        ) {
            with(context).message(resId).position(position).duration(duration).show()
        }

        private fun findActivity(context: Context): Activity? {
            var ctx = context
            while (ctx is ContextWrapper) {
                if (ctx is Activity) return ctx
                ctx = ctx.baseContext
            }
            return null
        }
    }
}
