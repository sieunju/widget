package hmju.widget.flexible

/**
 * Description: Flexible State Data Class
 * 각 초기값은
 * Scale 1.0F
 * Focus 0F
 * Rotate 0F
 * Flip 1F
 * Created by juhongmin on 11/21/21
 */
data class FlexibleStateItem(
    var scale: Float,
    var focusX: Float,
    var focusY: Float,
    var rotationDegree: Float,
    var flipX: Float,
    var flipY: Float
) {

    val scaleX: Float
        get() = scale * flipX
    val scaleY: Float
        get() = scale * flipY
    var minScale: Float = -1F
    var imgWidth: Int = -1
    var imgHeight: Int = -1

    val currentImgWidth: Float
        get() = if(imgWidth == -1) -1F else imgWidth * scale
    val currentImgHeight: Float
        get() = if(imgHeight == -1) -1F else imgHeight * scale

    fun reset() {
        scale = if (minScale != -1F) {
            minScale
        } else {
            1.0F
        }
        focusX = 0F
        focusY = 0F
        rotationDegree = 0F
        flipX = 1F
        flipY = 1F
    }
}