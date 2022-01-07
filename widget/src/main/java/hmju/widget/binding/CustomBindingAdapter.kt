package hmju.widget.binding

import android.view.View
import android.view.ViewGroup
import androidx.annotation.Dimension
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.tablayout.LinePagerTabLayout
import hmju.widget.tablayout.PagerTabItem
import hmju.widget.tablayout.PagerTabType

/**
 * Description : CustomBindingAdapter
 *
 * Created by juhongmin on 2022/01/07
 */
object CustomBindingAdapter {

    /**
     * set Layout Width or Height
     */
    @JvmStatic
    @BindingAdapter(value = ["layout_width", "layout_height"], requireAll = false)
    fun setLayoutWidthAndHeight(
        view: View,
        @Dimension width: Int?,
        @Dimension height: Int?
    ) {
        val layoutParams = view.layoutParams
        width?.run {
            layoutParams.width = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        height?.run {
            layoutParams.height = when (this) {
                -1 -> ViewGroup.LayoutParams.MATCH_PARENT
                -2 -> ViewGroup.LayoutParams.WRAP_CONTENT
                else -> this
            }
        }

        view.layoutParams = layoutParams
    }

    @JvmStatic
    @BindingAdapter("isSelected")
    fun View.setSelected(
        isSelected: Boolean?
    ) {
        this.isSelected = isSelected == true
    }

    /**
     * LinePagerTabLayout Set DataList 처리 함수
     * @param viewPager CurrentViewPager
     * @param type 탭 레이아웃 타입
     * @param dataList 탭 리스트
     * @param fixedSize 고정으로 가득 채울 사이즈
     */
    @JvmStatic
    @BindingAdapter(value = ["viewPager", "type", "menuList", "fixedSize"], requireAll = false)
    fun setLineTabDataList(
        view: LinePagerTabLayout,
        viewPager: ViewPager2,
        type: PagerTabType?,
        dataList: List<PagerTabItem>?,
        fixedSize: Int?
    ) {
        view.viewPager = viewPager
        view.fixedSize = fixedSize ?: -1
        view.setDataList(type ?: PagerTabType.DEFAULT, dataList)
    }
}