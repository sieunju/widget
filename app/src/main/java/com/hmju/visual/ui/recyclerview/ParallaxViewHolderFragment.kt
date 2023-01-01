package com.hmju.visual.ui.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.ExampleThumb
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.extensions.Extensions.getDeviceWidth
import hmju.widget.recyclerview.ParallaxView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ParallaxViewHolderFragment : Fragment(R.layout.f_parallax) {

    sealed class UiModel {
        data class Header(val title: String) : UiModel()
        data class Contents(val contents: String = "") : UiModel()
    }

    private lateinit var rvContents: RecyclerView
    private val adapter: Adapter by lazy { Adapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            rvContents = findViewById(R.id.rvContents)
            rvContents.layoutManager = LinearLayoutManager(view.context)
            rvContents.adapter = adapter
        }

        handleDataList()
    }

    private fun handleDataList() {
        lifecycleScope.launch(Dispatchers.Main) {
            val newList = withContext(Dispatchers.IO) {
                val dataList = mutableListOf<UiModel>()
                val longText = requireContext().getString(R.string.str_long_txt)
                for (idx in 0 until 50) {
                    if (idx % 5 == 0) {
                        dataList.add(UiModel.Header("Tag $idx"))
                    } else {
                        dataList.add(UiModel.Contents(longText))
                    }
                }
                return@withContext dataList
            }
            adapter.setDataList(newList)
        }
    }


    inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 100
        private val TYPE_CONTENTS = 101

        private val dataList: MutableList<UiModel> by lazy { mutableListOf() }

        fun setDataList(newList: List<UiModel>?) {
            if (newList == null) return

            dataList.clear()
            dataList.addAll(newList)
            notifyItemRangeChanged(0, itemCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_HEADER) {
                return HeaderViewHolder(parent)
            } else {
                return ContentViewHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
            if (dataList.size > pos) {
                val item = dataList[pos]
                if (holder is HeaderViewHolder) {
                    holder.onBindView(item)
                } else if (holder is ContentViewHolder) {
                    holder.onBindView(item)
                }
            }
        }

        override fun getItemViewType(pos: Int): Int {
            return if (dataList[pos] is UiModel.Header) {
                TYPE_HEADER
            } else {
                TYPE_CONTENTS
            }
        }

        override fun getItemCount() = dataList.size

        inner class HeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.vh_parallax_header, parent, false)
        ) {
            private val requestManager: RequestManager by lazy { Glide.with(this@ParallaxViewHolderFragment) }
            private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }
            private val parallaxView: ParallaxView by lazy { itemView.findViewById(R.id.parallax) }
            private val imgThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.imgThumb) }
            private val vAlpha: View by lazy { itemView.findViewById(R.id.vAlpha) }

            private val listener: ParallaxView.Listener = object : ParallaxView.Listener {
                /**
                 * 아래 기준으로 View 위치에 따라서 0.0 ~ 0.9999
                 * 전달하는 리스너
                 * @param percent 0.0 ~ 0.9999
                 */
                override fun onPercent(percent: Float) {
                    tvTitle.alpha = percent
                    vAlpha.alpha = 1.0F - (percent + 0.2F)
                }
            }

            init {
                parallaxView.setListener(listener)
            }

            fun onBindView(data: UiModel) {
                if (data is UiModel.Header) {
                    tvTitle.text = data.title
                    requestManager
                        .load(ExampleThumb.PARALLAX_HEADER)
                        .override(itemView.context.getDeviceWidth(), 200.dp)
                        .into(imgThumb)
                }
            }
        }

        inner class ContentViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.vh_parallax_contents, parent, false)
        ) {
            fun onBindView(data: UiModel) {

            }
        }
    }
}