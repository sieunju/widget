package hmju.widget.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import hmju.widget.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Description:  실시간 진행률을 바로 보여주게 하는 SurfaceView 기반의
 * ProgressView
 * Created by juhongmin on 6/13/21
 */
class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "ProgressView"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private val surfaceHolder: SurfaceHolder by lazy { holder }

    // [s] Attributes Value Define
    enum class Type {
        HORIZONTAL, VERTICAL
    }

    // 프로그래스 색상 및 라운딩 처리에 대한 정보 구조체.
    internal class GradientInfo {
        var radius: Int = 0
        var startColor: Int = 0
        var centerColor: Int = 0
        var endColor: Int = 0
        var location: Float = 0F
    }

    internal val gradientInfo: GradientInfo by lazy { GradientInfo() }
    internal var type = Type.HORIZONTAL  // default horizontal
    internal var max: Int = 100                // default Value 100
    internal var bgColor: Int = Color.BLACK    // default Black
    // [e] Attributes Value Define

    private var thread: ProgressThread? = null
    var currentProgress: Int = 0
        set(value) {
            if (field != value) {
                field = value
                if (currentLife == Life.CAN_DRAW) {
                    thread?.draw()
                }
            }
        }


    // Surface Life Cycle
    internal enum class Life {
        CAN_DRAW, NOT_DRAW
    }

    private var currentLife = Life.NOT_DRAW

    init {
        if (!isInEditMode) {
            context.obtainStyledAttributes(attrs, R.styleable.ProgressView).run {
                try {
                    bgColor = getColor(R.styleable.ProgressView_progressBgColor, Color.BLACK)
                    type = Type.values()[getInt(R.styleable.ProgressView_progressType, 0)]
                    max = getInt(R.styleable.ProgressView_progressMax, 100)
                    // set Start Progress
                    currentProgress = getInt(R.styleable.ProgressView_progressMin, 0)

                    gradientInfo.apply {
                        radius = getDimensionPixelOffset(R.styleable.ProgressView_progressRadius, 0)
                        startColor =
                            getColor(R.styleable.ProgressView_progressStartColor, Color.BLACK)
                        centerColor =
                            getColor(R.styleable.ProgressView_progressCenterColor, Color.BLACK)
                        endColor = getColor(R.styleable.ProgressView_progressEndColor, Color.BLACK)
                        location = getFloat(R.styleable.ProgressView_progressCenterXY, 0F)
                    }

                } catch (_: Exception) {

                }
                recycle()
            }
        }
        // 기본 SurfaceView 투명화
        setZOrderOnTop(true)

        surfaceHolder.apply {
            addCallback(this@ProgressView)
            setFormat(PixelFormat.TRANSPARENT)
        }
    }

    fun setType(type: Type): ProgressView {
        this.type = type
        return this
    }

    fun setMin(min: Int): ProgressView {
        currentProgress = min
        return this
    }

    fun setBgColor(id: Int): ProgressView {
        bgColor = id
        return this
    }

    fun setRadius(radius: Int): ProgressView {
        gradientInfo.radius = radius
        return this
    }

    fun setStartColor(color: Int): ProgressView {
        gradientInfo.startColor = color
        return this
    }

    fun setCenterColor(color: Int): ProgressView {
        gradientInfo.centerColor = color
        return this
    }

    fun setEndColor(color: Int): ProgressView {
        gradientInfo.endColor = color
        return this
    }

    fun setGradientLocation(location: Float): ProgressView {
        gradientInfo.location = location
        return this
    }

    /**
     * 프로그래스 증가율을 노출하는 함수.
     * {@link Reference #ProgressBar.incrementProgressBy(int}
     * @param diff 증가율
     * @author hmju
     */
    fun incrementProgressBy(diff: Int) {
        currentProgress += diff
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (thread != null) {
            thread?.closeThread()
            thread = null
        }

        // init Thread.
        currentLife = Life.CAN_DRAW
        thread = ProgressThread(width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        currentLife = Life.NOT_DRAW
        thread?.closeThread()
    }


    /**
     * ProgressThread Class
     *
     * @author hmju
     */
    internal inner class ProgressThread(
        private val surfaceWidth: Float,
        private val surfaceHeight: Float
    ) : Runnable {
        private val thread: ExecutorService by lazy {
            Executors.newFixedThreadPool(1).apply {
                execute(this@ProgressThread)
            }
        }
        private val fgPaint: Paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
                shader = if (type == Type.HORIZONTAL) {
                    LinearGradient(
                        0F, 0F, 0F, surfaceHeight,
                        intArrayOf(
                            gradientInfo.startColor,
                            gradientInfo.centerColor,
                            gradientInfo.endColor
                        ),
                        floatArrayOf(0F, gradientInfo.location, 1F), Shader.TileMode.CLAMP
                    )
                } else {
                    LinearGradient(
                        0F, 0F, 0F, surfaceHeight,
                        intArrayOf(
                            gradientInfo.startColor,
                            gradientInfo.centerColor,
                            gradientInfo.endColor
                        ),
                        floatArrayOf(0F, gradientInfo.location, 1F), Shader.TileMode.CLAMP
                    )
                }
            }
        }

        private val bgPaint: Paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
            }
        }

        private val bgRectF: RectF by lazy {
            RectF(
                0F,
                0F,
                surfaceWidth,
                surfaceHeight
            )
        }
        private val radius: Float by lazy { gradientInfo.radius.toFloat() }
        private val fgRectF: RectF by lazy {
            if (type == Type.HORIZONTAL) {
                RectF(0F, 0F, 0F, surfaceHeight)
            } else {
                RectF(0F, surfaceHeight, surfaceWidth, surfaceHeight)
            }
        }

        init {
            draw()
        }

        /**
         * Run Draw.
         * @author hmju
         */
        override fun run() {
            surfaceHolder.lockCanvas().runCatching {
                drawRoundRect(bgRectF, radius, radius, bgPaint)

                if (currentProgress == 0) {
                    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    return@runCatching this
                }

                if (type == Type.HORIZONTAL) {
                    val updateValue: Float =
                        (surfaceWidth * (currentProgress.toFloat()) / max.toFloat())
                    fgRectF.right = updateValue
                } else {
                    val updateValue: Float =
                        (((max - currentProgress) * surfaceHeight) / max)
                    fgRectF.top = updateValue
                }

                drawRect(fgRectF, fgPaint)
                return@runCatching this
            }.also {
                surfaceHolder.unlockCanvasAndPost(it.getOrNull())
            }
        }

        /**
         * Run Draw
         * @author hmmu
         */
        internal fun draw() {
            runCatching {
                this.run()
            }.onFailure {
                LogD("Draw Error\t${it.message}")
            }
        }

        /**
         * Call ShutDown Thread
         * @author hmju
         */
        internal fun closeThread() {
            runCatching {
                thread.shutdownNow()
            }.onFailure {
                LogD("CloseThread Error\t${it.message}")
            }
        }
    }
}