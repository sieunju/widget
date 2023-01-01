package com.hmju.visual.ui.select

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.hmju.visual.MainActivity.Companion.moveToFragment
import com.hmju.visual.MenuThumb
import com.hmju.visual.R
import com.hmju.visual.ui.coordinator.TranslationBehaviorActivity
import com.hmju.visual.ui.gesture.FlexibleImageViewFragment
import com.hmju.visual.ui.progress.ProgressFragment
import com.hmju.visual.ui.recyclerview.ParallaxViewHolderFragment
import com.hmju.visual.ui.recyclerview.RecyclerViewScrollerFragment
import com.hmju.visual.ui.recyclerview.SpecialGridDecorationFragment
import com.hmju.visual.ui.tablayout.CustomTabLayoutFragment
import com.hmju.visual.ui.view.CustomViewFragment
import com.hmju.visual.ui.viewpager.ViewPagerFragment
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
        val targetFragment: KClass<out Fragment>? = null,
        val targetActivity: KClass<out FragmentActivity>? = null
    ) {
        companion object {

            fun toActivity(
                title: String,
                imageThumb: String,
                activity: KClass<out FragmentActivity>
            ): MenuUiModel {
                return MenuUiModel(title, imageThumb, targetActivity = activity)
            }

            fun toFragment(
                title: String,
                imageThumb: String,
                fragment: KClass<out Fragment>
            ): MenuUiModel {
                return MenuUiModel(title, imageThumb, targetFragment = fragment)
            }
        }
    }

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
            MenuUiModel.toFragment(
                "CustomView",
                MenuThumb.VIEW,
                CustomViewFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "Gesture-FlexibleImageEditView",
                MenuThumb.FLEXIBLE,
                FlexibleImageViewFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "ProgressView",
                MenuThumb.PROGRESS,
                ProgressFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "ViewPager-LineIndicator",
                MenuThumb.VIEWPAGER,
                ViewPagerFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "ViewPager-TabLayout",
                MenuThumb.TAB_LAYOUT,
                CustomTabLayoutFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "RecyclerView-ParallaxViewHolder",
                MenuThumb.PARALLAX,
                ParallaxViewHolderFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "RecyclerView-SpecialGrid",
                MenuThumb.SPECIAL_GRID_DECORATION,
                SpecialGridDecorationFragment::class
            )
        )
        list.add(
            MenuUiModel.toFragment(
                "RecyclerView-Scroller",
                MenuThumb.RECYCLERVIEW_CUSTOM_SCROLLER,
                RecyclerViewScrollerFragment::class
            )
        )
        list.add(
            MenuUiModel.toActivity(
                "Coordinator-TranslationBehavior",
                MenuThumb.TRANSLATION_BEHAVIOR,
                TranslationBehaviorActivity::class
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
                    val targetFragment = targetFragment
                    val targetActivity = targetActivity
                    // 선택한 Fragment or Activity 이동
                    if (targetFragment != null) {
                        fragment.parentFragmentManager.moveToFragment(targetFragment)
                    } else if (targetActivity != null) {
                        Intent(itemView.context, targetActivity.java).apply {
                            startActivity(this)
                        }
                    }
                }
            }
        }

        fun onBindView(model: MenuUiModel) {
            this.model = model
            tvTitle.text = model.title
            val imageThumb = model.imageThumb
            if (!imageThumb.isNullOrEmpty()) {
                if (imageThumb.endsWith(".webp")) {
                    requestManager.load(imageThumb)
                        .optionalTransform(
                            WebpDrawable::class.java,
                            WebpDrawableTransformation(FitCenter())
                        )
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(ivThumb)
                } else {
                    requestManager.load(imageThumb)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(ivThumb)
                }

            } else {
                ivThumb.visibility = View.GONE
            }
        }
    }
}
