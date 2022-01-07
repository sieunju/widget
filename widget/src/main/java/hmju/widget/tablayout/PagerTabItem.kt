package hmju.widget.tablayout

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import hmju.widget.extensions.dp

/**
 * Description :
 *
 * Created by juhongmin on 2022/01/07
 */
data class PagerTabItem(
    val title: String,
    @DrawableRes var icon: Int? = null,
    var pos: Int = 0,
    @Dimension var iconWidth: Int? = null,
    @Dimension var txtSize: Int = 16.dp,
    @ColorInt var txtColor: Int = Color.BLACK,
    @ColorInt var disableTxtColor: Int = Color.BLACK,
    @ColorInt var bgColor: Int = Color.WHITE,
    @ColorInt var disableBgColor: Int = Color.WHITE,
    var isChangeTextStyle: Boolean = false
) {
    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }
}