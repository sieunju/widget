package hmju.widget.view.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 * Description : [sourceView] 가 그리는 내용을 동일한 화면 좌표로 보정하여
 * 그대로 다시 그려주는 View.
 *
 * 헤더 영역 위에 겹쳐두고 [RenderEffectBlurStrategy] 와 함께 사용하면
 * 뒤에 흐르는 RecyclerView 콘텐츠를 실시간으로 흐림 처리한 것처럼 보여줄 수 있다.
 *
 * 주의 : [sourceView] 는 이 View 의 하위 View 가 되어서는 안 된다.
 * (자기 자신을 다시 그리는 무한 재귀에 빠질 수 있음)
 *
 * Created by juhongmin on 2026. 6. 15.
 */
class BlurMirrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * 이 View 에 그대로 미러링되어 그려질 대상 View.
     * [BlurStrategy.setup] 에서 지정되고 [BlurStrategy.release] 에서 해제된다.
     */
    var sourceView: View? = null

    private val srcLocation = IntArray(2)
    private val dstLocation = IntArray(2)

    init {
        // RenderEffect 적용을 위해 하드웨어 가속 레이어 필수
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * 현재 [sourceView] 가 그리는 내용을 [Bitmap] 으로 캡처한다.
     * 블러 적용 전 원본 이미지를 확인하는 디버그용.
     */
    fun captureFrame(): Bitmap? {
        val source = sourceView ?: return null
        if (source.width == 0 || source.height == 0) return null
        val bitmap = Bitmap.createBitmap(
            width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888
        )
        val bitmapCanvas = Canvas(bitmap)
        source.getLocationInWindow(srcLocation)
        getLocationInWindow(dstLocation)
        val dx = (srcLocation[0] - dstLocation[0]).toFloat()
        val dy = (srcLocation[1] - dstLocation[1]).toFloat()
        bitmapCanvas.save()
        bitmapCanvas.translate(dx, dy)
        source.draw(bitmapCanvas)
        bitmapCanvas.restore()
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val source = sourceView ?: return
        if (source.width == 0 || source.height == 0) return

        source.getLocationInWindow(srcLocation)
        getLocationInWindow(dstLocation)

        val dx = (srcLocation[0] - dstLocation[0]).toFloat()
        val dy = (srcLocation[1] - dstLocation[1]).toFloat()

        canvas.save()
        canvas.translate(dx, dy)
        source.draw(canvas)
        canvas.restore()
    }
}
