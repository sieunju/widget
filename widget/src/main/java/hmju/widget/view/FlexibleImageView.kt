package hmju.widget.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import hmju.widget.ImageLoader
import hmju.widget.flexible.FlexibleStateItem
import hmju.widget.flexible.decector.MoveGestureDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*


/**
 * Description : 이동, 확대가 가능한 ImageView
 *
 * Created by juhongmin on 11/21/21
 */
class FlexibleImageView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(ctx, attrs, defStyleAttr) {
    companion object {
        const val MAX_SCALE_FACTOR = 10.0F
        const val MIN_SCALE_FACTOR = 0.3F
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
        MoveGestureDetector(MoveListener())
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

    /**
     * Load Url Http or content://
     * @param url Request Url
     */
    fun loadUrl(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val bitmap = if (url.startsWith("http")) {
                ImageLoader.loadBitmapHttp(url)
            } else {
                ImageLoader.loadBitmapFile(context, url)
            }
            loadBitmap(bitmap)
        }
    }

    /**
     * Load Bitmap
     * @param bitmap Image Bitmap
     */
    fun loadBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return

        GlobalScope.launch(Dispatchers.IO) {
            resetView()
            withContext(Dispatchers.Main) {
                val pair = cropBitmap(bitmap)
                // 비트맵의 주소값이 서로 다른 경우 Recycle 처리
                if (bitmap !== pair.first && !bitmap.isRecycled) {
                    bitmap.recycle()
                }

                stateItem.imgWidth = (pair.first.width.toFloat() / pair.second).toInt()
                stateItem.imgHeight = (pair.first.height.toFloat() / pair.second).toInt()
                stateItem.scale = pair.second
                stateItem.startScale = pair.second
                stateItem.minScale = 1F
                setImageBitmap(pair.first)
            }
        }
    }

    /**
     * 이미지를 가운데로 옮기고 꽉차게 처리
     * UiThread 에서 이 함수를 실행 해야 합니다.
     */
    @MainThread
    fun centerCrop() {
        // 애니메이션 처리 유무 검사
        if (stateItem.imgWidth == 0 || stateItem.imgHeight == 0 ||
            stateItem.scale == stateItem.startScale
        ) return

        ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, stateItem.startScale),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, stateItem.startScale),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0F),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F)
        ).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addListener(
                onStart = {
                    isTouchLock = true
                },
                onEnd = {
                    stateItem.scale = this@FlexibleImageView.scaleX
                    stateItem.focusX = this@FlexibleImageView.translationX
                    stateItem.focusY = this@FlexibleImageView.translationY
                    invalidate()
                    isTouchLock = false
                }
            )
            start()
        }
    }

    /**
     * 이미지를 가운데로 옮기고
     * UiThread 에서 이 함수를 실행 해야 합니다.
     */
    @MainThread
    fun fitCenter() {
        // 애니메이션 처리 유무 검사
        if (stateItem.imgWidth == 0 || stateItem.imgHeight == 0 || stateItem.scale == stateItem.minScale) return

        ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, stateItem.minScale),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, stateItem.minScale),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0F),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F)
        ).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addListener(
                onStart = {
                    isTouchLock = true
                },
                onEnd = {
                    stateItem.scale = this@FlexibleImageView.scaleX
                    stateItem.focusX = this@FlexibleImageView.translationX
                    stateItem.focusY = this@FlexibleImageView.translationY
                    invalidate()
                    isTouchLock = false
                }
            )
            start()
        }
    }

    /**
     * View 및 데이터 리셋 처리 함수
     */
    private fun resetView() {
        stateItem.reset()
        isMultiTouch = false
        moveDistance = 0.0
        touchPoint = PointF()
        alpha = 1F
    }

    /**
     * Get Row Point
     * @param ev 터이 이벤트!
     * @param index 터치한 인덱스
     * @param point Current Point
     */
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
            super.performClick()
        }
    }

    override fun performLongClick(): Boolean {
        return if (isMultiTouch || moveDistance > MAX_LONG_CLICK_DISTANCE) {
            false
        } else {
            super.performLongClick()
        }
    }

    /**
     * 클릭 이벤트 계산 처리 함수
     */
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
        if (translationX != stateItem.focusX) {
            translationX = stateItem.focusX
        }
        if (translationY != stateItem.focusY) {
            translationY = stateItem.focusY
        }
        if (scaleX != stateItem.scaleX) {
            scaleX = stateItem.scaleX
        }
        if (scaleY != stateItem.scaleY) {
            scaleY = stateItem.scaleY
        }

        // rotation = stateItem.rotationDegree 회전은 나중에 처리 할 예정
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
    }

    /**
     * 이미지 View 너비 높이에 맞게 줄이거나 늘리는 처리 함수
     * @param bitmap Source Bitmap
     * @return 알맞게 Scale 한 비트맵, 해당 비트맵과 뷰의 Max Scale 값
     */
    private suspend fun cropBitmap(bitmap: Bitmap): Pair<Bitmap, Float> {
        return withContext(Dispatchers.Default) {
            var xScale: Float = viewWidth.toFloat() / bitmap.width.toFloat()
            var yScale: Float = viewHeight.toFloat() / bitmap.height.toFloat()
            // 가장 큰 비율 가져옴
            var maxScale = Math.max(xScale, yScale)

            val scaledWidth = maxScale * bitmap.width
            val scaledHeight = maxScale * bitmap.height

            xScale = scaledWidth / viewWidth.toFloat()
            yScale = scaledHeight / viewHeight.toFloat()
            maxScale = Math.max(xScale, yScale)
            Bitmap.createScaledBitmap(
                bitmap,
                scaledWidth.toInt(),
                scaledHeight.toInt(),
                true
            ) to maxScale
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    override fun setScaleType(scaleType: ScaleType?) {
        super.setScaleType(ScaleType.FIT_CENTER)
    }

    /**
     * Image 위치값 연산 처리 함수.
     * 이미지 현재 너비 와 높이 값과 현재 포커싱 잡힌 X,Y 값을 기준으로
     * Top, Left, Right, Bottom 값들을 구할수 있다.
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


        // 좌우 모자란 부분이 있는 경우 -> X 좌표 가운데
        // MinScale 이 1.0 이하인 경우에만 존재
        if ((rect.left > 0 && rect.right < viewWidth) && (rect.top > 0 && rect.bottom < viewHeight)) {
            // 0으로 초기화
            return Pair(-stateItem.focusX, -stateItem.focusY)
        }

        if (rect.width() < viewWidth) {
            // 좌우 공간이 부족한 경우
            diffFocusX = -stateItem.focusX
        } else if (rect.left > 0) {
            // 왼쪽에 빈공간이 있는 경우
            diffFocusX = -Math.abs(rect.left)
        } else if (rect.right < viewWidth) {
            // 오른쪽에 빈공간이 있는 경우
            diffFocusX = Math.abs(rect.right - viewWidth)
        }

        if (rect.height() < viewHeight) {
            // 상하 공간이 부족한 경우
            diffFocusY = -stateItem.focusY
        } else if (rect.top > 0) {
            // 위쪽에 빈공간이 있는 경우
            diffFocusY = -Math.abs(rect.top)
        } else if (rect.bottom < viewHeight) {
            // 아래쪽에 빈공간이 있는 경우
            diffFocusY = Math.abs(rect.bottom - viewHeight)
        }

        return Pair(diffFocusX, diffFocusY)
    }

    private fun getDistance(point1: PointF, point2: PointF): Double {
        return sqrt(
            (point1.x - point2.x).toDouble().pow(2.0) + (point1.y - point2.y).toDouble()
                .pow(2.0)
        )
    }

    /**
     * 제 위치로 가기 위한 애니메이션 처리 함수
     * @param targetX Target X 좌표
     * @param targetY Target Y 좌표
     */
    private fun handleTargetTranslation(targetX: Float, targetY: Float) {
        // LogD("Ani $targetX $targetY")
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

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        var prevScale = 0.5F

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = stateItem.scale * detector.scaleFactor

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
                fitCenter()
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
            computeImageLocation()?.let { rect ->
                val pair = computeInBoundary(rect) ?: return

                handleTargetTranslation(
                    stateItem.focusX.plus(pair.first),
                    stateItem.focusY.plus(pair.second)
                )
            }
        }
    }
}