package hmju.widget.layoutmanager

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import hmju.widget.layoutmanager.SpecialGridItemDecoration.Companion.spanSpecialGridType

/**
 * Description : SpecialGridItemDecoration 전용 Binding Adapter
 *
 * Created by juhongmin on 2022/03/21
 */
object SpecialDecorationBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["specialGridTypeList"])
    fun RecyclerView.setSpanSpecialGridType(
        gridTypeList: List<Int>?
    ) {
        if (gridTypeList == null || gridTypeList.isEmpty()) return
        spanSpecialGridType(gridTypeList, 2)
    }
}
