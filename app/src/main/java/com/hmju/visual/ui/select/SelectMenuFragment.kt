package com.hmju.visual.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hmju.visual.MainActivity.Companion.moveToFragment
import com.hmju.visual.MenuThumb
import com.hmju.visual.R
import com.hmju.visual.ui.coordinator.TranslationBehaviorFragment
import com.hmju.visual.ui.gesture.FlexibleImageViewFragment
import com.hmju.visual.ui.progress.ProgressFragment
import com.hmju.visual.ui.recyclerview.ParallaxViewHolderFragment
import com.hmju.visual.ui.tablayout.CustomTabLayoutFragment
import com.hmju.visual.ui.view.CustomViewFragment
import com.hmju.visual.ui.viewpager.ViewPagerFragment
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * Description : Visual Ui Selection Menu Fragment
 *
 * Created by juhongmin on 2022/12/30
 */
internal class SelectMenuFragment : Fragment(R.layout.f_select_menu) {

    data class MenuUiModel(
        val title: String,
        val imageThumb: String? = null,
        val targetFragment: KClass<out Fragment>
    )

    private lateinit var rvContents: RecyclerView

    private val adapter: Adapter by lazy { Adapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.adapter = adapter
        adapter.setDataList(getMenuList())
    }

    private fun getMenuList(): List<MenuUiModel> {
        val list = mutableListOf<MenuUiModel>()
        list.add(
            MenuUiModel(
                "CustomView",
                MenuThumb.VIEW,
                CustomViewFragment::class
            )
        )
        list.add(
            MenuUiModel(
                "Gesture-FlexibleImageEditView",
                MenuThumb.FLEXIBLE,
                FlexibleImageViewFragment::class
            )
        )
        list.add(MenuUiModel("ProgressView", MenuThumb.PROGRESS, ProgressFragment::class))
        list.add(
            MenuUiModel(
                "ViewPager-LineIndicator",
                MenuThumb.VIEWPAGER,
                ViewPagerFragment::class
            )
        )
        list.add(MenuUiModel("ViewPager-TabLayout", MenuThumb.TAB_LAYOUT, CustomTabLayoutFragment::class))
        list.add(
            MenuUiModel(
                "RecyclerView-ParallaxViewHolder",
                targetFragment = ParallaxViewHolderFragment::class
            )
        )
        list.add(
            MenuUiModel(
                "Coordinator-TranslationBehavior",
                targetFragment = TranslationBehaviorFragment::class
            )
        )
        return list
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        private val dataList: MutableList<MenuUiModel> by lazy { mutableListOf() }

        inner class DiffUtilCallback(
            private val oldList: List<MenuUiModel>,
            private val newList: List<MenuUiModel>
        ) : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem == newItem
            }
        }

        fun setDataList(newList: List<MenuUiModel>? = null) {
            if (newList == null) return
            val diffUtil = DiffUtil.calculateDiff(DiffUtilCallback(dataList, newList))
            dataList.clear()
            dataList.addAll(newList)
            diffUtil.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent, this@SelectMenuFragment)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                if (dataList.size > position) {
                    holder.onBindView(dataList[position])
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
    }

    inner class ViewHolder(parent: ViewGroup, fragment: Fragment) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_child_select_menu, parent, false)
    ) {
        private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }
        private val ivThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.ivThumb) }
        private var model: MenuUiModel? = null
        private val requestManager: RequestManager by lazy { Glide.with(fragment) }

        init {
            itemView.setOnClickListener {
                model?.runCatching {
                    // 선택한 Fragment 이동
                    fragment.parentFragmentManager.moveToFragment(targetFragment)
                }?.onFailure {
                    Timber.d("ERROR $it")
                }
            }
        }

        fun onBindView(model: MenuUiModel) {
            this.model = model
            tvTitle.text = model.title
            val imageThumb = model.imageThumb
            if (!imageThumb.isNullOrEmpty()) {
                requestManager.load(imageThumb)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(ivThumb)
            } else {
                ivThumb.visibility = View.GONE
            }
        }
    }
}
