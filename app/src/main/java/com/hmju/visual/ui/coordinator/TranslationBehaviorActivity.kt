package com.hmju.visual.ui.coordinator

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.hmju.visual.Constants.ExampleThumb.PARALLAX_HEADER
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.extensions.Extensions.getDeviceWidth
import hmju.widget.recyclerview.ParallaxView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TranslationBehaviorActivity : AppCompatActivity() {

    sealed class UiModel(@LayoutRes val layoutId: Int) {
        data class Header(val title: String) : UiModel(R.layout.vh_parallax_header)
        data class Contents(val contents: String = "") : UiModel(R.layout.vh_parallax_contents)
    }

    private lateinit var rvContents: RecyclerView
    private lateinit var tvBeforeTitle: AppCompatTextView
    private lateinit var tvAfterTitle: AppCompatTextView

    private val adapter: Adapter by lazy { Adapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_translation_behavior)

        initView()

        tvBeforeTitle.text = "Hello"
        tvAfterTitle.text = "It's Me..."
        handleDataList()
    }

    private fun initView() {
        window.statusBarColor = Color.TRANSPARENT
        rvContents = findViewById(R.id.rvContents)
        rvContents.layoutManager = LinearLayoutManager(this)
        rvContents.adapter = adapter
        tvBeforeTitle = findViewById(R.id.tvBeforeTitle)
        tvAfterTitle = findViewById(R.id.tvAfterTitle)
    }

    private fun handleDataList() {
        lifecycleScope.launch(Dispatchers.Main) {
            val newList = withContext(Dispatchers.IO) {
                val dataList = mutableListOf<UiModel>()
                val longText = getString(R.string.str_long_txt)
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

        private val dataList: MutableList<UiModel> by lazy { mutableListOf() }

        fun setDataList(newList: List<UiModel>?) {
            if (newList == null) return

            dataList.clear()
            dataList.addAll(newList)
            notifyItemRangeChanged(0, itemCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == R.layout.vh_parallax_header) {
                HeaderViewHolder(parent)
            } else {
                ContentViewHolder(parent)
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
            return if (dataList.size > pos) {
                dataList[pos].layoutId
            } else {
                super.getItemViewType(pos)
            }
        }

        override fun getItemCount() = dataList.size

        inner class HeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.vh_parallax_header, parent, false)
        ) {
            private val requestManager: RequestManager by lazy { Glide.with(this@TranslationBehaviorActivity) }
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
                        .load(PARALLAX_HEADER)
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
