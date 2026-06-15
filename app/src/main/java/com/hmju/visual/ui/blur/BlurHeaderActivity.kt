package com.hmju.visual.ui.blur

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.Constants.ExampleThumb
import com.hmju.visual.LogoThumb
import com.hmju.visual.R
import com.hmju.visual.databinding.ABlurHeaderBinding
import com.hmju.visual.databinding.VhChildDynamicCoordinatorBinding
import hmju.widget.view.BlurStrategy
import hmju.widget.view.DimOnlyStrategy
import hmju.widget.view.RenderEffectBlurStrategy
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

    private val blurStrategy: BlurStrategy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RenderEffectBlurStrategy()
    } else {
        DimOnlyStrategy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_blur_header)
        initAdapter()
        initBlur()
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
        val adapter = Adapter()
        binding.rvContents.layoutManager = LinearLayoutManager(this)
        binding.rvContents.adapter = adapter
        lifecycleScope.launch {
            val dataList = (0..20).toList()
                .map { Card("Index $it", dummyImages[Random.nextInt(dummyImages.size)]) }
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
    }

    data class Card(
        val title: String,
        val imageUrl: String
    )

    private class SimpleDiffUtil : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }

    inner class Adapter : ListAdapter<Card, Adapter.ViewHolder>(SimpleDiffUtil()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBindView(getItem(position))
        }

        inner class ViewHolder(
            parent: ViewGroup
        ) : RecyclerView.ViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.vh_child_dynamic_coordinator, parent, false)
        ) {
            private val binding: VhChildDynamicCoordinatorBinding by lazy {
                VhChildDynamicCoordinatorBinding.bind(itemView)
            }

            fun onBindView(item: Card) {
                reqManager.load(item.imageUrl)
                    .into(binding.ivThumb)
                binding.tvTitle.text = item.title
            }
        }
    }
}
