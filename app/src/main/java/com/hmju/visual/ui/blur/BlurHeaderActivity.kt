package com.hmju.visual.ui.blur

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.Constants.ExampleThumb
import com.hmju.visual.LogoThumb
import com.hmju.visual.R
import com.hmju.visual.databinding.ABlurHeaderBinding
import com.hmju.visual.databinding.VhChildDynamicCoordinatorBinding
import com.hmju.visual.databinding.VhSpecialLinear1Binding
import hmju.widget.view.blur.BlurStrategy
import hmju.widget.view.blur.Extensions.create
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Description : [hmju.widget.view.BlurMirrorView] 동작 확인용 테스트 화면.
 * RecyclerView 를 스크롤하면 상단 헤더 영역이 뒤에 흐르는 콘텐츠를 실시간으로 흐림 처리한다.
 *
 * Created by juhongmin on 2026. 6. 15.
 */
internal class BlurHeaderActivity : AppCompatActivity() {

    private lateinit var binding: ABlurHeaderBinding
    private val reqManager: RequestManager by lazy { Glide.with(this) }

    private val blurStrategy: BlurStrategy = create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_blur_header)
        initAdapter()
        initBlur()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, { v, insets ->
            val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            v.updatePadding(bottom = navHeight)
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        })
    }

    override fun onDestroy() {
        blurStrategy.release()
        super.onDestroy()
    }

    private fun initAdapter() {
        val dummyImages = listOf(
            LogoThumb.LOGO,
            LogoThumb.LOGO_PURPLE,
            ExampleThumb.GALAXY,
            ExampleThumb.PARALLAX_HEADER,
            ExampleThumb.DEEP_LINK_WALLPAPER
        )
        val adapter = Adapter(reqManager)
        binding.rvContents.adapter = adapter
        lifecycleScope.launch {
            val dataList = buildList {
                (0..20).forEach { index ->
                    if (index % 5 == 0) {
                        add(ListItem.Text("Section가나다라마자사아자차카타파하하하하하하 ${index / 5 + 1}"))
                    }
                    add(
                        ListItem.Image(
                            "Index $index",
                            dummyImages[Random.nextInt(dummyImages.size)]
                        )
                    )
                }
            }
            adapter.submitList(dataList)
        }
    }

    private fun initBlur() {
        blurStrategy.setup(binding.blurMirror, binding.rvContents)
        binding.rvContents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                blurStrategy.onScroll()
            }
        })
        initBlurControl()
    }

    private fun initBlurControl() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // DimOnlyStrategy 는 반경 개념이 없어 컨트롤이 의미가 없으므로 숨긴다.
            binding.llBlurControl.isVisible = false
            return
        }
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.tvRadiusX.text = "Radius X : ${binding.sbRadiusX.progress}"
                binding.tvRadiusY.text = "Radius Y : ${binding.sbRadiusY.progress}"
                blurStrategy.setRadius(
                    binding.sbRadiusX.progress.coerceAtLeast(1).toFloat(),
                    binding.sbRadiusY.progress.coerceAtLeast(1).toFloat()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        }
        binding.sbRadiusX.setOnSeekBarChangeListener(listener)
        binding.sbRadiusY.setOnSeekBarChangeListener(listener)
    }

    sealed interface ListItem {
        data class Image(
            val title: String,
            val imageUrl: String
        ) : ListItem

        data class Text(
            val content: String
        ) : ListItem
    }

    private class SimpleDiffUtil : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }

    private class Adapter(
        private val reqManager: RequestManager
    ) : ListAdapter<ListItem, Adapter.ViewHolder>(SimpleDiffUtil()) {
        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is ListItem.Image -> VIEW_TYPE_IMAGE
                is ListItem.Text -> VIEW_TYPE_TEXT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                VIEW_TYPE_TEXT -> TextViewHolder(parent)
                else -> ImageViewHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (holder) {
                is ImageViewHolder -> holder.onBindView(getItem(position) as ListItem.Image)
                is TextViewHolder -> holder.onBindView(getItem(position) as ListItem.Text)
            }
        }

        sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        inner class ImageViewHolder(
            parent: ViewGroup
        ) : ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.vh_child_dynamic_coordinator, parent, false)
        ) {
            private val binding: VhChildDynamicCoordinatorBinding by lazy {
                VhChildDynamicCoordinatorBinding.bind(itemView)
            }

            fun onBindView(item: ListItem.Image) {
                reqManager.load(item.imageUrl)
                    .into(binding.ivThumb)
                binding.tvTitle.text = item.title
            }
        }

        inner class TextViewHolder(
            parent: ViewGroup
        ) : ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.vh_special_linear_1, parent, false)
        ) {
            private val binding: VhSpecialLinear1Binding by lazy {
                VhSpecialLinear1Binding.bind(itemView)
            }

            fun onBindView(item: ListItem.Text) {
                binding.tvTitle.text = item.content
            }
        }

        companion object {
            private const val VIEW_TYPE_IMAGE = 0
            private const val VIEW_TYPE_TEXT = 1
        }
    }
}
