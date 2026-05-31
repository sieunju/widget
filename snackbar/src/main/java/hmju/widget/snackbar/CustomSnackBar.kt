package hmju.widget.snackbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Description : Activity / Fragment 레이어에서 Top / Bottom 위치 지정 및
 * 연속 이벤트 처리를 지원하는 커스텀 SnackBar.
 *
 * Style:
 *  - FLOAT  : 둥글둥글한 플로팅 스타일 (기본값)
 *  - BANNER : Full-width, TOP 은 Status Bar 오버랩
 *
 * 연속 이벤트: dismiss / show 독립 동시 실행
 *
 * Created by juhongmin
 */
class CustomSnackBar private constructor(
    private val activity: Activity
) {

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    enum class Position { TOP, BOTTOM }

    enum class Style {
        /** 둥글둥글한 플로팅 카드 — 흰 배경, Corner 16, 여백 16dp, 그림자 */
        FLOAT,
        /** 풀 와이드 배너 — TOP 은 StatusBar 오버랩, BOTTOM 은 NavBar 오버랩 */
        BANNER
    }

    fun interface OnDismissListener { fun onDismissed() }
    fun interface OnShowListener { fun onShown() }

    private var message: CharSequence = ""
    private var duration: Long = 2500L
    private var position: Position = Position.BOTTOM
    private var style: Style = Style.FLOAT
    private var onDismissListener: OnDismissListener? = null
    private var onShowListener: OnShowListener? = null

    fun message(msg: CharSequence): CustomSnackBar { message = msg; return this }
    fun message(@StringRes resId: Int): CustomSnackBar { message = activity.getString(resId); return this }
    fun duration(ms: Long): CustomSnackBar { duration = ms; return this }
    fun position(pos: Position): CustomSnackBar { position = pos; return this }
    fun style(s: Style): CustomSnackBar { style = s; return this }
    fun setOnDismissListener(l: OnDismissListener): CustomSnackBar { onDismissListener = l; return this }
    fun setOnShowListener(l: OnShowListener): CustomSnackBar { onShowListener = l; return this }

    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return
        Controller.show(activity, this)
    }

    fun dismiss() = Controller.dismiss(activity)

    // ─────────────────────────────────────────────────────────────────────────
    // State Machine
    // ─────────────────────────────────────────────────────────────────────────

    private class State {
        var snackBar: CustomSnackBar? = null
        var entry: SnackEntry? = null
        var autoDismissJob: Job? = null
    }

    private object Controller {
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private val states = HashMap<Int, State>()

        fun show(activity: Activity, snackBar: CustomSnackBar) {
            val key = System.identityHashCode(activity)
            val state = states.getOrPut(key) { State() }
            cancelAutoDismiss(state)

            // 기존 SnackBar → dismiss 독립 실행 (show 를 블로킹하지 않음)
            val oldEntry = state.entry
            val oldSnackBar = state.snackBar
            if (oldEntry != null) {
                oldEntry.dismissWithAnimation {
                    oldSnackBar?.onDismissListener?.onDismissed()
                }
            }

            // 신규 SnackBar 즉시 show (dismiss 완료를 기다리지 않음 → 2개 동시 화면 존재)
            doShow(key, state, snackBar)
        }

        fun dismiss(activity: Activity) {
            val key = System.identityHashCode(activity)
            val state = states[key] ?: return
            val entry = state.entry ?: return
            val snackBar = state.snackBar
            cancelAutoDismiss(state)
            state.entry = null
            state.snackBar = null
            entry.dismissWithAnimation {
                snackBar?.onDismissListener?.onDismissed()
                if (states[key]?.entry == null) states.remove(key)
            }
        }

        private fun doShow(key: Int, state: State, snackBar: CustomSnackBar) {
            val activity = snackBar.activity
            if (activity.isFinishing || activity.isDestroyed) { states.remove(key); return }

            val decorView = activity.window.decorView as ViewGroup
            val entry = SnackEntry.create(
                decorView = decorView,
                message = snackBar.message,
                position = snackBar.position,
                style = snackBar.style
            )
            state.snackBar = snackBar
            state.entry = entry

            entry.showWithAnimation { snackBar.onShowListener?.onShown() }

            state.autoDismissJob = scope.launch {
                delay(snackBar.duration)
                val s = states[key] ?: return@launch
                if (s.entry === entry) {
                    s.entry = null
                    s.snackBar = null
                    entry.dismissWithAnimation {
                        snackBar.onDismissListener?.onDismissed()
                        if (states[key]?.entry == null) states.remove(key)
                    }
                }
            }
        }

        private fun cancelAutoDismiss(state: State) {
            state.autoDismissJob?.cancel()
            state.autoDismissJob = null
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
        private var dismissCalled = false

        companion object {
            private const val ANIM_MS = 250L
            private val SLIDE_IN = DecelerateInterpolator()
            private val SLIDE_OUT = AccelerateInterpolator()

            private const val CORNER_DP = 16f
            private const val PADDING_DP = 16f
            private const val MARGIN_DP = 16f

            fun create(
                decorView: ViewGroup,
                message: CharSequence,
                position: Position,
                style: Style
            ): SnackEntry {
                val ctx = decorView.context
                val dp = ctx.resources.displayMetrics.density

                val windowInsets = decorView.rootWindowInsets
                val statusBarHeight = windowInsets?.systemWindowInsetTop ?: 0
                val navBarHeight = windowInsets?.systemWindowInsetBottom ?: 0

                val snackView = when (style) {
                    Style.FLOAT -> buildFloatView(ctx, dp, message)
                    Style.BANNER -> buildBannerView(ctx, dp, message, position, statusBarHeight, navBarHeight)
                }

                val params = when (style) {
                    Style.FLOAT -> buildFloatParams(dp, position, statusBarHeight, navBarHeight)
                    Style.BANNER -> buildBannerParams(position)
                }

                decorView.addView(snackView, params)
                return SnackEntry(snackView, decorView, position)
            }

            // ── FLOAT ──────────────────────────────────────────────────────

            private fun buildFloatView(
                ctx: android.content.Context,
                dp: Float,
                message: CharSequence
            ) = FrameLayout(ctx).apply {
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = CORNER_DP * dp
                }
                elevation = 8f * dp
                val pad = (PADDING_DP * dp).toInt()
                addView(
                    TextView(ctx).apply {
                        text = message
                        setTextColor("#1A1A1A".toColorInt())
                        textSize = 14f
                        setPadding(pad, pad, pad, pad)
                    },
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            private fun buildFloatParams(
                dp: Float,
                position: Position,
                statusBarHeight: Int,
                navBarHeight: Int
            ) = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val hMargin = (MARGIN_DP * dp).toInt()
                val vMargin = (12 * dp).toInt()
                marginStart = hMargin
                marginEnd = hMargin
                when (position) {
                    Position.TOP -> {
                        gravity = Gravity.TOP
                        topMargin = statusBarHeight + vMargin
                    }
                    Position.BOTTOM -> {
                        gravity = Gravity.BOTTOM
                        bottomMargin = navBarHeight + vMargin
                    }
                }
            }

            // ── BANNER ─────────────────────────────────────────────────────

            private fun buildBannerView(
                ctx: android.content.Context,
                dp: Float,
                message: CharSequence,
                position: Position,
                statusBarHeight: Int,
                navBarHeight: Int
            ) = FrameLayout(ctx).apply {
                val corner = CORNER_DP * dp
                // TOP: 상단 코너 0 (status bar 오버랩), 하단 코너 16dp
                // BOTTOM: 상단 코너 16dp, 하단 코너 0 (nav bar 오버랩)
                val radii = when (position) {
                    Position.TOP -> floatArrayOf(0f, 0f, 0f, 0f, corner, corner, corner, corner)
                    Position.BOTTOM -> floatArrayOf(corner, corner, corner, corner, 0f, 0f, 0f, 0f)
                }
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadii = radii
                }
                elevation = 12f * dp

                val hPad = (PADDING_DP * dp).toInt()
                val vPad = (PADDING_DP * dp).toInt()
                val topPad = if (position == Position.TOP) statusBarHeight + vPad else vPad
                val botPad = if (position == Position.BOTTOM) navBarHeight + vPad else vPad

                addView(
                    TextView(ctx).apply {
                        text = message
                        setTextColor("#1A1A1A".toColorInt())
                        textSize = 14f
                        setPadding(hPad, topPad, hPad, botPad)
                    },
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            private fun buildBannerParams(position: Position) =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = when (position) {
                        Position.TOP -> Gravity.TOP
                        Position.BOTTOM -> Gravity.BOTTOM
                    }
                }
        }

        fun showWithAnimation(onShown: () -> Unit) {
            view.post {
                if (dismissCalled) return@post
                view.translationY = slideOffset()
                view.alpha = 0f
                view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(ANIM_MS)
                    .setInterpolator(SLIDE_IN)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) { onShown() }
                    })
                    .start()
            }
        }

        fun dismissWithAnimation(onDismissed: () -> Unit) {
            dismissCalled = true
            view.animate().cancel()
            if (view.alpha == 0f) {
                parent.removeView(view)
                onDismissed()
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
                        onDismissed()
                    }
                })
                .start()
        }

        private fun slideOffset(): Float {
            val h = view.height.takeIf { it > 0 } ?: 200
            return if (position == Position.BOTTOM) h.toFloat() else -h.toFloat()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {

        @JvmStatic
        fun with(activity: Activity): CustomSnackBar = CustomSnackBar(activity)

        @JvmStatic
        fun with(fragment: Fragment): CustomSnackBar {
            val snackBar = CustomSnackBar(fragment.requireActivity())
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) snackBar.dismiss()
            }
            fragment.lifecycle.addObserver(observer)
            return snackBar
        }

        @JvmStatic
        @JvmOverloads
        fun show(
            activity: Activity,
            message: CharSequence,
            position: Position = Position.BOTTOM,
            style: Style = Style.FLOAT,
            duration: Long = 2500L
        ) {
            with(activity).message(message).position(position).style(style).duration(duration).show()
        }

        @JvmStatic
        @JvmOverloads
        fun show(
            fragment: Fragment,
            message: CharSequence,
            position: Position = Position.BOTTOM,
            style: Style = Style.FLOAT,
            duration: Long = 2500L
        ) {
            with(fragment).message(message).position(position).style(style).duration(duration).show()
        }
    }
}
