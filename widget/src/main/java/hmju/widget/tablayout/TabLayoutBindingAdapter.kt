package hmju.widget.tablayout

import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter

object TabLayoutBindingAdapter {

    @JvmStatic
    @BindingAdapter("tabTextStyle")
    fun setTabTextStyle(
        tv: AppCompatTextView,
        @StyleRes style: Int
    ) {
        TextViewCompat.setTextAppearance(tv, style)
    }
}
