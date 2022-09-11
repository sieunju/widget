package hmju.widget.tablayout

import android.view.View
import androidx.annotation.StyleRes
import androidx.lifecycle.MutableLiveData

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

    // 해당 탭 선택 유부 LiveData
    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }
}