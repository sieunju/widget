package hmju.widget.view.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.withTranslation
import androidx.core.graphics.createBitmap

/**
 * Description : [sourceView] 가 그리는 내용을 동일한 화면 좌표로 보정하여
 * 그대로 다시 그려주는 View.
 *
 * 헤더 영역 위에 겹쳐두고 [RenderEffectBlurStrategy] 와 함께 사용하면
 * 뒤에 흐르는 RecyclerView 콘텐츠를 실시간으로 흐림 처리한 것처럼 보여줄 수 있다.
 *
 * [backgroundView] 를 지정하면 [sourceView] 아래에 배경을 먼저 합성한 뒤 블러를 적용한다.
 * RecyclerView 의 배경색을 임의로 변경하지 않고 실제 배경 View 기준으로 블러를 걸고 싶을 때 사용한다.
 *
 * 주의 : [sourceView], [backgroundView] 는 이 View 의 하위 View 가 되어서는 안 된다.
 * (자기 자신을 다시 그리는 무한 재귀에 빠질 수 있음)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
class BlurMirrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _sourceView: View? = null
    val sourceView: View? get() = _sourceView

    private var _backgroundView: View? = null
    val backgroundView: View? get() = _backgroundView

    fun setSourceView(view: View?) {
        _sourceView = view
    }

    fun setBackgroundView(view: View?) {
        _backgroundView = view
    }

    private val srcLocation = IntArray(2)
    private val dstLocation = IntArray(2)

    init {
        // RenderEffect 적용을 위해 하드웨어 가속 레이어 필수
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * 현재 [_backgroundView] + [_sourceView] 가 그리는 내용을 [Bitmap] 으로 캡처한다.
     * 블러 적용 전 원본 합성 이미지를 확인하는 디버그용.
     */
    fun captureFrame(): Bitmap? {
        val source = _sourceView ?: return null
        if (source.width == 0 || source.height == 0) return null
        val bitmap = createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1))
        val bitmapCanvas = Canvas(bitmap)
        getLocationInWindow(dstLocation)

        _backgroundView?.let { bg ->
            if (bg.width > 0 && bg.height > 0) {
                bg.getLocationInWindow(srcLocation)
                bitmapCanvas.withTranslation(
                    (srcLocation[0] - dstLocation[0]).toFloat(),
                    (srcLocation[1] - dstLocation[1]).toFloat()
                ) { bg.draw(this) }
            }
        }

        source.getLocationInWindow(srcLocation)
        bitmapCanvas.withTranslation(
            (srcLocation[0] - dstLocation[0]).toFloat(),
            (srcLocation[1] - dstLocation[1]).toFloat()
        ) { source.draw(this) }

        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val source = sourceView ?: return
        getLocationInWindow(dstLocation)
        Log.d("DevJu","onDraw ${source}")
        backgroundView?.let { bg ->
            if (bg.width > 0 && bg.height > 0) {
                bg.getLocationInWindow(srcLocation)
                canvas.withTranslation(
                    (srcLocation[0] - dstLocation[0]).toFloat(),
                    (srcLocation[1] - dstLocation[1]).toFloat()
                ) { bg.draw(this) }
            }
        }

        if (source.width == 0 || source.height == 0) return
        source.getLocationInWindow(srcLocation)
        canvas.withTranslation(
            (srcLocation[0] - dstLocation[0]).toFloat(),
            (srcLocation[1] - dstLocation[1]).toFloat()
        ) { source.draw(this) }
    }
}
