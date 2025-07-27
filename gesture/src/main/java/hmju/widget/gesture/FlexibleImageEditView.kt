package hmju.widget.gesture

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import hmju.widget.gesture.decetor.MoveGestureDetector
import kotlin.math.*


/**
 * Description : 이동, 확대가 가능한 ImageView
 *
 * Created by juhongmin on 11/21/21
 */
@Suppress("unused")
class FlexibleImageEditView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(ctx, attrs, defStyleAttr) {

    companion object {
        const val MAX_SCALE_FACTOR = 10.0F
        const val MIN_SCALE_FACTOR = 0.3F
        const val MAX_LONG_CLICK_DISTANCE = 16

        private const val TAG = "FlexibleImageView"
        private const val DEBUG = false
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }
    }

    interface Listener {
        fun onUpdateStateItem(newItem: FlexibleStateItem)
    }

    private val scaleGestureDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(ctx, ScaleListener())
    }

    private val moveGestureDetector: MoveGestureDetector by lazy {
        MoveGestureDetector(MoveListener())
    }

    private val stateItem: FlexibleStateItem by lazy { FlexibleStateItem() }

    private var isMultiTouch: Boolean = false
    private var moveDistance: Double = 0.0
    private var touchPoint = PointF()
    private var isTouchLock: Boolean = false // 애니메이션 동작중 터치 잠금하기위한 Flag 값

    private var listener: Listener? = null

    init {
        if (isInEditMode) {
            setBackgroundColor(Color.BLACK)
        }
    }

    /**
     * Load Bitmap
     * @param bitmap Image Bitmap
     */
    @MainThread
    fun loadBitmap(bitmap: Bitmap?) {
        loadBitmap(bitmap, null)
    }

    /**
     * Load Bitmap
     * @param bitmap Image Bitmap
     * @param newItem Target FlexibleStateItem
     */
    @MainThread
    fun loadBitmap(bitmap: Bitmap?, newItem: FlexibleStateItem? = null) {
        if (bitmap == null) return

        resetView()

        if (newItem != null) {
            setImageBitmap(bitmap)
            stateItem.run {
                focusX = newItem.focusX
                focusY = newItem.focusY
                imgWidth = newItem.imgWidth
                imgHeight = newItem.imgHeight
                scale = newItem.scale
                startScale = newItem.startScale
                minScale = newItem.minScale
                viewWidth = newItem.viewWidth
                viewHeight = newItem.viewHeight
            }
            invalidate()
        } else {
            val pair = cropBitmap(bitmap)
            stateItem.run {
                imgWidth = (pair.first.width.toFloat() / pair.second).toInt()
                imgHeight = (pair.first.height.toFloat() / pair.second).toInt()
                scale = pair.second
                startScale = pair.second
                minScale = 1F
            }

            setImageBitmap(pair.first)
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
        ) {
            return
        }

        ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, stateItem.startScale),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, stateItem.startScale),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0F),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F)
        ).apply {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
            addListener(
                onStart = {
                    isTouchLock = true
                },
                onEnd = {
                    stateItem.scale = this@FlexibleImageEditView.scaleX
                    stateItem.focusX = this@FlexibleImageEditView.translationX
                    stateItem.focusY = this@FlexibleImageEditView.translationY
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
        if (stateItem.imgWidth == 0 || stateItem.imgHeight == 0 ||
            stateItem.scale == stateItem.minScale
        ) {
            return
        }

        ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, stateItem.minScale),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, stateItem.minScale),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0F),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F)
        ).apply {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
            addListener(
                onStart = {
                    isTouchLock = true
                },
                onEnd = {
                    stateItem.scale = this@FlexibleImageEditView.scaleX
                    stateItem.focusX = this@FlexibleImageEditView.translationX
                    stateItem.focusY = this@FlexibleImageEditView.translationY
                    invalidate()
                    isTouchLock = false
                }
            )
            start()
        }
    }

    /**
     * 해당 이미지의 비트맵과 현재까지 이동한 좌표에 대한 정보를
     * 리턴하는 함수
     * @return 이미지의 현재 좌표
     */
    fun getStateItem(): RectF? {
        return computeImageLocation()
    }

    /**
     * 해당 이미지가 위치한 State Item Model
     */
    fun getFlexibleStateItem() = stateItem

    /**
     * 해당 이미지 비트맵 Getter 처리함수
     */
    @Throws(IllegalArgumentException::class)
    fun getImageBitmap(): Bitmap {
        if (drawable is BitmapDrawable) {
            return (drawable as BitmapDrawable).bitmap
        } else {
            throw IllegalArgumentException("Not Bitmap Drawable")
        }
    }

    /**
     * set Listener
     */
    fun setListener(listener: Listener) {
        this.listener = listener
    }

    /**
     * View 및 데이터 리셋 처리 함수
     */
    private fun resetView() {
        stateItem.reset()
        isMultiTouch = false
        moveDistance = 0.0
        touchPoint = PointF()
    }

    /**
     * Get Row Point
     * @param ev 터치 이벤트!
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

    @SuppressLint("Recycle", "ClickableViewAccessibility")
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

    override fun onDraw(canvas: Canvas) {
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
        onDelegatedListener()
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        stateItem.viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        stateItem.viewHeight = MeasureSpec.getSize(heightMeasureSpec)
    }


    /**
     * 이미지 View 너비 높이에 맞게 줄이거나 늘리는 처리 함수
     * @param bitmap Source Bitmap
     * @return 알맞게 Scale 한 비트맵, 해당 비트맵과 뷰의 Max Scale 값
     */
    private fun cropBitmap(bitmap: Bitmap): Pair<Bitmap, Float> {
        var xScale: Float = stateItem.viewWidth.toFloat() / bitmap.width.toFloat()
        var yScale: Float = stateItem.viewHeight.toFloat() / bitmap.height.toFloat()
        // 가장 큰 비율 가져옴
        var maxScale = xScale.coerceAtLeast(yScale)

        val scaledWidth = maxScale * bitmap.width
        val scaledHeight = maxScale * bitmap.height

        xScale = scaledWidth / stateItem.viewWidth.toFloat()
        yScale = scaledHeight / stateItem.viewHeight.toFloat()
        maxScale = xScale.coerceAtLeast(yScale)
        return Bitmap.createScaledBitmap(
            bitmap,
            scaledWidth.toInt(),
            scaledHeight.toInt(),
            true
        ) to maxScale
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
    internal fun computeImageLocation(): RectF? {
        if (stateItem.viewWidth == -1 || stateItem.viewHeight == -1 ||
            stateItem.currentImgWidth == -1F ||
            stateItem.currentImgHeight == -1F
        ) return null

        val imgWidth = stateItem.currentImgWidth
        val imgHeight = stateItem.currentImgHeight
        val focusX = stateItem.focusX
        val focusY = stateItem.focusY

        val imgTop = (focusY + (stateItem.viewHeight / 2F)) - imgHeight / 2F
        val imgLeft = (focusX + (stateItem.viewWidth / 2F)) - imgWidth / 2F
        val imgRight = (focusX + (stateItem.viewWidth / 2F)) + imgWidth / 2F
        val imgBottom = (focusY + (stateItem.viewHeight / 2F)) + imgHeight / 2F
        return RectF(imgLeft, imgTop, imgRight, imgBottom)
    }

    /**
     * View 영역 밖으로 나갔는지 유무 함수.
     * @param rect Current Image Location
     */
    private fun computeInBoundary(rect: RectF): Pair<Float, Float> {
        var diffFocusX = 0F
        var diffFocusY = 0F

        // 좌우 모자란 부분이 있는 경우 -> X 좌표 가운데
        // MinScale 이 1.0 이하인 경우에만 존재
        if ((rect.left > 0 && rect.right < stateItem.viewWidth) && (rect.top > 0 && rect.bottom < stateItem.viewHeight)) {
            // 0으로 초기화
            return Pair(-stateItem.focusX, -stateItem.focusY)
        }

        when {
            rect.width() < stateItem.viewWidth -> {
                // 좌우 공간이 부족한 경우
                diffFocusX = -stateItem.focusX
            }

            rect.left > 0 -> {
                // 왼쪽에 빈공간이 있는 경우
                diffFocusX = -abs(rect.left)
            }

            rect.right < stateItem.viewWidth -> {
                // 오른쪽에 빈공간이 있는 경우
                diffFocusX = abs(rect.right - stateItem.viewWidth)
            }
        }

        when {
            rect.height() < stateItem.viewHeight -> {
                // 상하 공간이 부족한 경우
                diffFocusY = -stateItem.focusY
            }

            rect.top > 0 -> {
                // 위쪽에 빈공간이 있는 경우
                diffFocusY = -abs(rect.top)
            }

            rect.bottom < stateItem.viewHeight -> {
                // 아래쪽에 빈공간이 있는 경우
                diffFocusY = abs(rect.bottom - stateItem.viewHeight)
            }
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
        val pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, targetX)
        val pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, targetY)
        ObjectAnimator.ofPropertyValuesHolder(this@FlexibleImageEditView, pvhX, pvhY).apply {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
            doOnStart { isTouchLock = true }
            doOnEnd {
                stateItem.focusX = this@FlexibleImageEditView.translationX
                stateItem.focusY = this@FlexibleImageEditView.translationY
                invalidate()
                isTouchLock = false
            }
            start()
        }
    }

    /**
     * 설정한 리스너에 FlexibleStateItem 전달하는 함수
     */
    private fun onDelegatedListener() {
        listener?.onUpdateStateItem(stateItem)
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var prevScale = 0.5F

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

        override fun onScaleEnd(detector: ScaleGestureDetector) {
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
                val pair = computeInBoundary(rect)

                handleTargetTranslation(
                    stateItem.focusX.plus(pair.first),
                    stateItem.focusY.plus(pair.second)
                )
            }
        }
    }
}