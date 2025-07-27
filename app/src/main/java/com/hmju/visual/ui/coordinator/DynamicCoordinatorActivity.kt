package com.hmju.visual.ui.coordinator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.appbar.AppBarLayout
import com.hmju.visual.Constants
import com.hmju.visual.Constants.ExampleThumb
import com.hmju.visual.LogoThumb
import com.hmju.visual.R
import com.hmju.visual.databinding.ADynamicCoordinatorBinding
import com.hmju.visual.databinding.VhChildDynamicCoordinatorBinding
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random


/**
 * Description :
 *
 * Created by juhongmin on 3/3/24
 */
internal class DynamicCoordinatorActivity : AppCompatActivity() {

    enum class State {
        EXPANDED,
        COLLAPSED
    }

    private lateinit var binding: ADynamicCoordinatorBinding
    private var dynamicContentsHeight = -1
    private var headerHeight = -1
    private var scrollTargetEnd = -1
    private val reqManager: RequestManager by lazy { Glide.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_dynamic_coordinator)
        binding.tvContents.post { dynamicContentsHeight = binding.tvContents.height }
        binding.llHeader.post {
            headerHeight = binding.llHeader.height
            scrollTargetEnd = (headerHeight.toFloat() / 2).toInt()
        }
        binding.abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var currentState: State? = null

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange
                val offset = abs(verticalOffset)
                if (offset == 0) {
                    currentState = State.EXPANDED
                } else if (offset >= totalRange) {
                    currentState = State.COLLAPSED
                }
                if (dynamicContentsHeight == -1 || headerHeight == -1) return

                if (offset <= dynamicContentsHeight && currentState == State.EXPANDED) {
                    val newOffset = getAdjustRangeValue(
                        standardValue = offset,
                        standardEnd = dynamicContentsHeight,
                        targetEnd = scrollTargetEnd
                    )
                    binding.llHeader.translationY = -newOffset
                } else {
                    if (currentState == State.COLLAPSED &&
                        offset in totalRange.minus(scrollTargetEnd)..totalRange
                    ) {
                        val newOffset = getAdjustRangeValue(
                            standardValue = offset,
                            standardStart = totalRange.minus(scrollTargetEnd),
                            standardEnd = totalRange,
                            targetEnd = scrollTargetEnd
                        )
                        binding.llHeader.translationY = -newOffset
                    }
                }
            }
        })
        initAdapter()
    }

    /**
     * 기준이 되는 범위, 위치와 값에 따라서
     * 새로운 범위에서 위치값을 리턴하는 함수
     * @param standardValue 기준이 되는 위치값
     * @param standardStart 기준이 되는 시작
     * @param standardEnd 기준이 되는 끝
     * @param targetStart 새로운 범위의 시작
     * @param targetEnd 새로운 범위의 끝
     */
    private fun getAdjustRangeValue(
        standardValue: Int,
        standardStart: Int = 0,
        standardEnd: Int,
        targetStart: Int = 0,
        targetEnd: Int
    ): Float {
        // FaN 이슈 대응
        if (standardEnd - standardStart == 0) return standardValue.toFloat()
        val standardStartF = standardStart.toFloat()
        val standardEndF = standardEnd.toFloat()
        val targetStartF = targetStart.toFloat()
        val targetEndF = targetEnd.toFloat()
        val ratio = (standardValue.minus(standardStartF)) / standardEndF.minus(standardStartF)
        return ratio * (targetEndF.minus(targetStartF)) + targetStartF
    }

    data class Card(
        val title: String,
        val imageUrl: String
    )

    private fun initAdapter() {
        val dummyImages = listOf(
            LogoThumb.LOGO,
            LogoThumb.LOGO_PURPLE,
            ExampleThumb.GALAXY,
            ExampleThumb.PARALLAX_HEADER,
            ExampleThumb.DEEP_LINK_WALLPAPER
        )
        val adapter = Adapter()
        binding.rvContents.adapter = adapter
        lifecycleScope.launch {
            val dataList = (0..10).toList()
                .map { Card("Index $it", dummyImages[Random.nextInt(dummyImages.size)]) }
            adapter.submitList(dataList)
        }
    }

    private class SimpleDiffUtil : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }

    inner class Adapter : ListAdapter<Card, Adapter.ViewHolder>(SimpleDiffUtil()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
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
                VhChildDynamicCoordinatorBinding.bind(
                    itemView
                )
            }

            fun onBindView(item: Card) {
                reqManager.load(item.imageUrl)
                    .into(binding.ivThumb)
                binding.tvTitle.text = item.title
            }
        }
    }
}
