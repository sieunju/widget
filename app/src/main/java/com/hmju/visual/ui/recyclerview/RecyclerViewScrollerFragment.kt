package com.hmju.visual.ui.recyclerview

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.recyclerview.scroller.CustomLinearScroller
import hmju.widget.view.CustomTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class RecyclerViewScrollerFragment : Fragment(R.layout.f_recyclerview_scroller) {

    inner class UiModel(val title: String, @ColorInt val color: Int) {
        var isSelected: Boolean = false
    }

    private lateinit var rvHorizontal: RecyclerView
    private lateinit var vpHorizontal: ViewPager2
    private lateinit var rvVertical: RecyclerView

    private val viewPagerAdapter: ViewPagerAdapter by lazy { ViewPagerAdapter() }
    private val horizontalAdapter: HorizontalRvAdapter by lazy { HorizontalRvAdapter() }
    private val horizontalScroller: CustomLinearScroller by lazy {
        CustomLinearScroller(requireContext()).apply {
            type = CustomLinearScroller.ScrollerType.CENTER
        }
    }

    private val verticalAdapter: VerticalRvAdapter by lazy { VerticalRvAdapter() }
    private val verticalScroller: CustomLinearScroller by lazy {
        CustomLinearScroller(requireContext()).apply {
            type = CustomLinearScroller.ScrollerType.START
            duration = 1000
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        handleViewPagerListener()
        setData()

        handleVerticalAutoPosition()
        handleHorizontalAutoPosition()
    }

    private fun initView(view: View) {
        rvHorizontal = view.findViewById(R.id.rvHorizontal)
        rvHorizontal.layoutManager =
            LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
        vpHorizontal = view.findViewById(R.id.vpHorizontal)
        rvVertical = view.findViewById(R.id.rvVertical)

        rvHorizontal.adapter = horizontalAdapter
        vpHorizontal.adapter = viewPagerAdapter

        rvVertical.layoutManager = LinearLayoutManager(view.context)
        rvVertical.adapter = verticalAdapter
        rvVertical.addItemDecoration(VerticalItemDecoration(
            divider = 15.dp
        ))
    }

    private fun setData() {
        val uiList = getHorizontalDataList()
        viewPagerAdapter.setDataList(uiList)
        horizontalAdapter.setDataList(uiList)
        verticalAdapter.setDataList(uiList)
    }

    private fun handleViewPagerListener() {
        vpHorizontal.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                val lm = rvHorizontal.layoutManager as LinearLayoutManager
                horizontalScroller.targetPosition = pos
                horizontalAdapter.setFocusingPosition(pos)
                lm.startSmoothScroll(horizontalScroller)
            }
        })
    }

    private fun handleVerticalAutoPosition() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeat(30) {
                delay(3000)
                val ranPosition = Random.nextInt(0, verticalAdapter.itemCount.minus(1))
                val lm = rvVertical.layoutManager as LinearLayoutManager
                verticalScroller.targetPosition = ranPosition
                lm.startSmoothScroll(verticalScroller)
            }
        }
    }

    private fun handleHorizontalAutoPosition(){
        lifecycleScope.launch(Dispatchers.Main) {
            repeat(300) {
                val pos = if (vpHorizontal.currentItem == viewPagerAdapter.itemCount.minus(1)) {
                    0
                } else {
                    vpHorizontal.currentItem.plus(1)
                }
                vpHorizontal.currentItem = pos
                delay(600)
            }
        }
    }

    private fun getHorizontalDataList(): List<UiModel> {
        val list = mutableListOf<UiModel>()
        list.add(UiModel("Black", Color.BLACK))
        list.add(UiModel("Gray", Color.GRAY))
        list.add(UiModel("Blue", Color.BLUE))
        list.add(UiModel("DKGRAY", Color.DKGRAY))
        list.add(UiModel("GREEN", Color.GREEN))
        list.add(UiModel("YELLOW", Color.YELLOW))
        list.add(UiModel("CYAN", Color.CYAN))
        list.add(UiModel("MAGENTA", Color.MAGENTA))
        return list
    }

    inner class ViewPagerAdapter : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

        private val dataList: MutableList<UiModel> by lazy { mutableListOf() }

        fun setDataList(newList: List<UiModel>?) {
            if (newList == null) return

            dataList.addAll(newList)
            notifyItemRangeInserted(0, itemCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (dataList.size > position) {
                holder.bindView(dataList[position])
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.vh_sample_custom_tab_layout, parent, false
            )
        ) {
            private val view = itemView.findViewById<View>(R.id.v)

            fun bindView(item: UiModel) {
                view.setBackgroundColor(item.color)
            }
        }
    }

    inner class HorizontalRvAdapter : RecyclerView.Adapter<HorizontalRvAdapter.ViewHolder>() {

        private val dataList: MutableList<UiModel> by lazy { mutableListOf() }

        fun setDataList(newList: List<UiModel>?) {
            if (newList == null) return

            dataList.addAll(newList)
            notifyItemRangeInserted(0, itemCount)
        }

        fun setFocusingPosition(pos: Int) {
            var updatePositions = 0
            for (idx in 0 until dataList.size) {
                if (dataList[idx].isSelected) {
                    updatePositions = idx
                    dataList[idx].isSelected = false
                    break
                }
            }
            dataList[pos].isSelected = true
            notifyItemChanged(updatePositions)
            notifyItemChanged(pos)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (dataList.size > position) {
                holder.bindView(dataList[position])
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.vh_child_tab, parent, false
            )
        ) {
            private val tvTitle = itemView.findViewById<CustomTextView>(R.id.tvTitle)

            fun bindView(item: UiModel) {
                tvTitle.text = item.title
                tvTitle.isSelected = item.isSelected
            }
        }
    }

    inner class VerticalRvAdapter : RecyclerView.Adapter<VerticalRvAdapter.ViewHolder>() {

        private val dataList: MutableList<UiModel> by lazy { mutableListOf() }

        fun setDataList(newList: List<UiModel>?) {
            if (newList == null) return

            dataList.addAll(newList)
            notifyItemRangeInserted(0, itemCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (dataList.size > position) {
                holder.bindView(dataList[position])
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.vh_special_linear_1, parent, false
            )
        ) {
            private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)

            init {
                itemView.updateLayoutParams {
                    height = 300.dp
                }
            }

            fun bindView(item: UiModel) {
                tvTitle.text = "${bindingAdapterPosition}_${item.title}"
            }
        }
    }
}
