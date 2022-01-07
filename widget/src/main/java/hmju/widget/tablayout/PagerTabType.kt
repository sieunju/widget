package hmju.widget.tablayout

import androidx.annotation.LayoutRes
import hmju.widget.R

/**
 * Description : PagerTabChild Layout Id EnumClass
 *
 * Created by juhongmin on 2022/01/07
 */
enum class PagerTabType(@LayoutRes val layoutId: Int) {
    DEFAULT(R.layout.v_child_line_default_tab_layout)
}