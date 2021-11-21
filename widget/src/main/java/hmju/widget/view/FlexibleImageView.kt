package hmju.widget.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import hmju.widget.flexible.FlexibleStateItem
import hmju.widget.flexible.decector.MoveGestureDetector
import kotlin.math.*

/**
 * Description : 이동, 확대가 가능한 ImageView
 *
 * Created by juhongmin on 11/21/21
 */
open class FlexibleImageView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(ctx, attrs, defStyleAttr) {
    companion object {
        const val MAX_SCALE_FACTOR = 10.0F
        const val MIN_SCALE_FACTOR = 0.3F
        const val MAX_CLICK_DISTANCE = 4
        const val MAX_LONG_CLICK_DISTANCE = 16

        private const val TAG = "FlexibleImageView"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    private val scaleGestureDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(ctx, ScaleListener())
    }

    private val moveGestureDetector: MoveGestureDetector by lazy {
        MoveGestureDetector(ctx, MoveListener())
    }

    var stateItem = FlexibleStateItem(
        scale = 1.0F,
        focusX = 0F,
        focusY = 0F,
        rotationDegree = 0F,
        flipX = 1F,
        flipY = 1F
    )

    private var isMultiTouch: Boolean = false
    private var moveDistance: Double = 0.0
    private var touchPoint = PointF()
    private var viewWidth = -1
    private var viewHeight = -1
    private var isTouchLock: Boolean = false // 애니메이션 동작중 터치 잠금하기위한 Flag 값

    init {
        if (isInEditMode) {
            setBackgroundColor(Color.BLACK)
        }
    }

    fun resetView() {
        stateItem.reset()
        isMultiTouch = false
        moveDistance = 0.0
        touchPoint = PointF()
        alpha = 1F
    }

    private fun getRowPoint(ev: MotionEvent, index: Int, point: PointF) {
        val location = intArrayOf(0, 0)
        getLocationOnScreen(location)

        var x = ev.getX(index)
        var y = ev.getY(index)

        x *= scaleX
        y *= scaleY

        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
        angle += rotation

        val length = PointF.length(x, y)
        x = (length * cos(Math.toRadians(angle)) + location[0]).toFloat()
        y = (length * sin(Math.toRadians(angle)) + location[1]).toFloat()

        point.set(x, y)
    }

    @SuppressLint("Recycle")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        } else if (isTouchLock) {
            return false
        }

        // compute trans from
        val prop = arrayOfNulls<MotionEvent.PointerProperties>(ev.pointerCount)
        val cords = arrayOfNulls<MotionEvent.PointerCoords>(ev.pointerCount)

        // get First Coords
        ev.getPointerCoords(0, MotionEvent.PointerCoords())

        for (i in 0 until ev.pointerCount) {
            val properties = MotionEvent.PointerProperties()
            ev.getPointerProperties(i, properties)
            prop[i] = properties

            val cod = MotionEvent.PointerCoords()
            ev.getPointerCoords(i, cod)

            val rawPos = PointF()
            getRowPoint(ev, i, rawPos)
            cod.x = rawPos.x
            cod.y = rawPos.y
            cords[i] = cod
        }

        val baseMotionEvent = MotionEvent.obtain(
            ev.downTime,
            ev.eventTime,
            ev.action,
            ev.pointerCount,
            prop,
            cords,
            ev.metaState,
            ev.buttonState,
            ev.xPrecision,
            ev.xPrecision,
            ev.deviceId,
            ev.edgeFlags,
            ev.source,
            ev.flags
        )

        scaleGestureDetector.onTouchEvent(baseMotionEvent)
        moveGestureDetector.onTouchEvent(baseMotionEvent)

        computeClickEvent(ev)
        super.onTouchEvent(ev)

        // Canvas Draw
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        return if (isMultiTouch || moveDistance > MAX_LONG_CLICK_DISTANCE) {
            false
        } else {
            super.performLongClick()
        }
    }

    override fun performLongClick(): Boolean {
        return if (isMultiTouch || moveDistance > MAX_LONG_CLICK_DISTANCE) {
            false
        } else {
            super.performLongClick()
        }
    }

    private fun computeClickEvent(ev: MotionEvent) {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                isMultiTouch = ev.pointerCount >= 2
                touchPoint = PointF(ev.rawX, ev.rawY)
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                if (ev.pointerCount > 1) {
                    isMultiTouch = true
                    return
                }

                moveDistance = getDistance(PointF(ev.rawX, ev.rawY), touchPoint)

            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        translationX = stateItem.focusX
        translationY = stateItem.focusY
        scaleY = stateItem.scaleX
        scaleX = stateItem.scaleY
        rotation = stateItem.rotationDegree
        // LogD("onDraw $stateItem")
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            stateItem.imgWidth = viewWidth
            stateItem.imgHeight = viewHeight
            stateItem.minScale = 1F
        }
    }

    /**
     * Image 위치값 연산 처리 함수.
     */
    private fun computeImageLocation(): RectF? {
        if (viewWidth == -1 || viewHeight == -1 ||
            stateItem.currentImgWidth == -1F ||
            stateItem.currentImgHeight == -1F
        ) return null

        val imgWidth = stateItem.currentImgWidth
        val imgHeight = stateItem.currentImgHeight
        val focusX = stateItem.focusX
        val focusY = stateItem.focusY

        val imgTop = (focusY + (viewHeight / 2F)) - imgHeight / 2F
        val imgLeft = (focusX + (viewWidth / 2F)) - imgWidth / 2F
        val imgRight = (focusX + (viewWidth / 2F)) + imgWidth / 2F
        val imgBottom = (focusY + (viewHeight / 2F)) + imgHeight / 2F
        return RectF(imgLeft, imgTop, imgRight, imgBottom)
    }

    /**
     * View 영역 밖으로 나갔는지 유무 함수.
     * @param rect Current Image Location
     */
    private fun computeInBoundary(rect: RectF): Pair<Float, Float>? {
        var diffFocusX = 0F
        var diffFocusY = 0F

        if (rect.left > 0) {
            diffFocusX -= Math.abs(rect.left)
        } else if (rect.right < viewWidth) {
            diffFocusX += Math.abs(rect.right - viewWidth)
        }

        if (rect.top > 0) {
            diffFocusY -= Math.abs(rect.top)
        } else if (rect.bottom < viewHeight) {
            diffFocusY += Math.abs(rect.bottom - viewHeight)
        }

        LogD("computeInBoundary $diffFocusX  $diffFocusY")

        // 변경점이 없으면 아래 로직 패스 한다.
        if (diffFocusX == 0F && diffFocusY == 0F) {
            return null
        }

        return Pair(diffFocusX, diffFocusY)
    }

    private fun getDistance(point1: PointF, point2: PointF): Double {
        return sqrt(
            (point1.x - point2.x).toDouble().pow(2.0) + (point1.y - point2.y).toDouble()
                .pow(2.0)
        )
    }


    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        var prevScale = 0.5F

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = stateItem.scale * detector.scaleFactor

            LogD("Scale $scale")
            prevScale = scale

            // 범위 를 넘어 가는 경우 false 리턴.
            if (scale <= MIN_SCALE_FACTOR || scale >= MAX_SCALE_FACTOR) {
                return false
            }

            stateItem.scale = scale

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            // 이미지 확대 축소 제한
            if (prevScale < stateItem.minScale) {
                LogD("확대 축소를 제자리로 합니다.")
                scaleTargetAni(stateItem.minScale)
            }
        }

        /**
         * 제 위치로 가기 위한 애니메이션 처리 함수
         * @param targetScale Target Scale 좌표
         */
        private fun scaleTargetAni(targetScale: Float) {
            val list = mutableListOf(
                PropertyValuesHolder.ofFloat(View.SCALE_X, targetScale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, targetScale),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X,0F),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,0F)
            )

            ObjectAnimator.ofPropertyValuesHolder(this@FlexibleImageView, *list.toList().toTypedArray()).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                doOnStart { isTouchLock = true }
                doOnEnd {
                    stateItem.scale = this@FlexibleImageView.scaleX
                    stateItem.focusX = this@FlexibleImageView.translationX
                    stateItem.focusY = this@FlexibleImageView.translationY
                    invalidate()
                    isTouchLock = false
                }
                start()
            }
        }
    }

    inner class MoveListener : MoveGestureDetector.Companion.SimpleOnMoveGestureListener() {

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            return true
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            val delta = detector.currentFocus
            stateItem.focusX += delta.x
            stateItem.focusY += delta.y
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            computeImageLocation()?.also { rect ->
                val pair = computeInBoundary(rect) ?: return

                focusTargetAni(
                    stateItem.focusX.plus(pair.first),
                    stateItem.focusY.plus(pair.second)
                )

//                if (pair.first != 0F) {
//                    stateItem.focusX += pair.first
//                }
//
//                if (pair.second != 0F) {
//                    stateItem.focusY += pair.second
//                }
//
//                // ReDraw
//                invalidate()
            }
        }

        /**
         * 제 위치로 가기 위한 애니메이션 처리 함수
         * @param targetX Target X 좌표
         * @param targetY Target Y 좌표
         */
        private fun focusTargetAni(targetX: Float, targetY: Float) {
            LogD("Ani $targetX $targetY")
            val pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, targetX)
            val pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, targetY)
            ObjectAnimator.ofPropertyValuesHolder(this@FlexibleImageView, pvhX, pvhY).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                doOnStart { isTouchLock = true }
                doOnEnd {
                    stateItem.focusX = this@FlexibleImageView.translationX
                    stateItem.focusY = this@FlexibleImageView.translationY
                    invalidate()
                    isTouchLock = false
                }
                start()
            }
        }
    }
}