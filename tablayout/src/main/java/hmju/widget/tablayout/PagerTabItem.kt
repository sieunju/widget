package hmju.widget.tablayout

import android.view.View
import androidx.annotation.StyleRes

/**
 * Description : Pager TabItem
 * @param title Tab Text
 * Created by juhongmin on 2021/12/31
 */
class PagerTabItem(
    val title: String
) {
    var pos: Int = 0

    @StyleRes
    var enableTextStyle: Int = View.NO_ID

    @StyleRes
    var disableTextStyle: Int = View.NO_ID

    var view: View? = null

    var isSelected: Boolean = false
}