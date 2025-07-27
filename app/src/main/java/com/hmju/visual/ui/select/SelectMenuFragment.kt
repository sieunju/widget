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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.hmju.visual.Constants
import com.hmju.visual.MainActivity.Companion.moveToFragment
import com.hmju.visual.R
import com.hmju.visual.ui.coordinator.DynamicCoordinatorActivity
import com.hmju.visual.ui.coordinator.TranslationBehaviorActivity
import com.hmju.visual.ui.gesture.FlexibleImageViewFragment
import com.hmju.visual.ui.progress.ProgressFragment
import com.hmju.visual.ui.recyclerview.ParallaxViewHolderFragment
import com.hmju.visual.ui.recyclerview.RecyclerViewScrollerFragment
import com.hmju.visual.ui.recyclerview.SpecialGridDecorationFragment
import com.hmju.visual.ui.view.StackCardViewFragment
import com.hmju.visual.ui.tablayout.CustomTabLayoutFragment
import com.hmju.visual.ui.view.CustomViewFragment
import com.hmju.visual.ui.viewpager.ViewPagerFragment
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import hmju.http.tracking_interceptor.TrackingHttpInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.reflect.KClass

/**
 * Description : Visual Ui Selection Menu Fragment
 *
 * Created by juhongmin on 2022/12/30
 */
internal class SelectMenuFragment : Fragment(R.layout.f_select_menu) {

    @OptIn(ExperimentalSerializationApi::class)
    private val apiService: GithubApiService by lazy {
        createRetrofit().create(GithubApiService::class.java)
    }

    data class MenuUiModel(
        val title: String,
        val imageThumb: String? = null,
        val targetFragment: KClass<out Fragment>? = null,
        val targetActivity: KClass<out FragmentActivity>? = null
    ) {
        constructor(entity: SelectionEntity) : this(
            title = entity.title,
            imageThumb = entity.imageUrl,
            targetFragment = when (entity.fragmentName) {
                "CustomViewFragment" -> CustomViewFragment::class
                "FlexibleImageViewFragment" -> FlexibleImageViewFragment::class
                "ProgressFragment" -> ProgressFragment::class
                "ViewPagerFragment" -> ViewPagerFragment::class
                "CustomTabLayoutFragment" -> CustomTabLayoutFragment::class
                "ParallaxViewHolderFragment" -> ParallaxViewHolderFragment::class
                "SpecialGridDecorationFragment" -> SpecialGridDecorationFragment::class
                "RecyclerViewScrollerFragment" -> RecyclerViewScrollerFragment::class
                "StackCardViewFragment" -> StackCardViewFragment::class
                else -> null
            },
            targetActivity = when (entity.activityName) {
                "TranslationBehaviorActivity" -> TranslationBehaviorActivity::class
                "DynamicCoordinatorActivity" -> DynamicCoordinatorActivity::class
                else -> null
            }
        )
    }

    private lateinit var rvContents: RecyclerView

    private val adapter: Adapter by lazy { Adapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = GridLayoutManager(view.context, 2)
        rvContents.adapter = adapter
        reqSelectionList()
    }

    @ExperimentalSerializationApi
    private fun createRetrofit(): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(TrackingHttpInterceptor())
            .build()
        val json = Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(httpClient)
            .build()
    }

    private fun reqSelectionList() {
        lifecycleScope.launch(Dispatchers.Main) {
            val list = withContext(Dispatchers.IO) {
                try {
                    val res = apiService.fetchSelection()
                    return@withContext res.list.map { MenuUiModel(it) }
                } catch (ex: Exception) {
                    return@withContext listOf()
                }
            }
            adapter.setDataList(list)
        }
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
