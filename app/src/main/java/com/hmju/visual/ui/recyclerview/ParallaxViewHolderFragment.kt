package com.hmju.visual.ui.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hmju.visual.ImageLoader
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.extensions.Extensions.getDeviceWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ParallaxViewHolderFragment : Fragment(R.layout.fragment_parallax) {

    sealed class UiModel {
        data class Header(val title: String) : UiModel()
        data class Contents(val contents: String = "") : UiModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            val longText = requireContext().getString(R.string.str_long_txt)
            val dataList = withContext(Dispatchers.Default) {
                val list = arrayListOf<UiModel>()
                for (idx in 0 until 50) {
                    if (idx % 5 == 0) {
                        list.add(UiModel.Header("Tag $idx"))
                    } else {
                        list.add(UiModel.Contents(longText))
                    }
                }
                return@withContext list
            }

            view.findViewById<RecyclerView>(R.id.rvContents).apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = Adapter(dataList)
            }
        }
    }


    class Adapter(private val list: ArrayList<UiModel>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 100
        private val TYPE_CONTENTS = 101

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_HEADER) {
                return HeaderViewHolder(parent)
            } else {
                return ContentViewHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
            if (holder is HeaderViewHolder) {
                holder.onBindView(list.get(pos))
            } else if (holder is ContentViewHolder) {
                holder.onBindView(list.get(pos))
            }
        }

        override fun getItemViewType(pos: Int): Int {
            if (list.get(pos) is UiModel.Header) {
                return TYPE_HEADER
            } else {
                return TYPE_CONTENTS
            }
        }

        override fun getItemCount() = list.size

        class HeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.parallax_header, parent, false)
        ) {
            private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }
            private val parallaxView: hmju.widget.recyclerview.ParallaxView by lazy { itemView.findViewById(
                R.id.parallax
            ) }
            private val imgThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.imgThumb) }
            private val vAlpha: View by lazy { itemView.findViewById(R.id.vAlpha) }
            private val tempImgUrl =
                "https://cdn.qtzz.synology.me/resource/img/20210921/1632238064795dwalkkz7dea.png"

            private val listener: hmju.widget.recyclerview.ParallaxView.Listener = object : hmju.widget.recyclerview.ParallaxView.Listener {
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
                GlobalScope.launch(Dispatchers.Main) {
                    imgThumb.setImageBitmap(
                        ImageLoader.imageBitmap(
                            tempImgUrl,
                            itemView.context.getDeviceWidth(),
                            200.dp
                        )
                    )
                }
            }

            fun onBindView(data: UiModel) {
                if (data is UiModel.Header) {
                    tvTitle.text = data.title
                }
            }
        }

        class ContentViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.parallax_contents, parent, false)
        ) {
            fun onBindView(data: UiModel) {

            }
        }
    }
}