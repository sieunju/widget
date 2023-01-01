package com.hmju.visual.ui.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.recyclerview.decoration.SpecialGridItemDecoration
import hmju.widget.recyclerview.decoration.SpecialGridItemDecoration.Companion.setSpanSpecialGridType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class SpecialGridDecorationFragment : Fragment(R.layout.f_special_grid) {

    data class SpecialItem(
        @LayoutRes val layoutId: Int
    )

    private val adapter: Adapter by lazy { Adapter() }

    private lateinit var rvContents: RecyclerView

    private val gridTypeList = listOf(R.layout.vh_special_grid_1)
    private val dummyList = mutableListOf<SpecialItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            rvContents = findViewById(R.id.rvContents)
            rvContents.layoutManager =
                GridLayoutManager(view.context, 2, RecyclerView.VERTICAL, false)
            rvContents.addItemDecoration(
                SpecialGridItemDecoration(
                    horizontalDivider = 20.dp,
                    verticalDivider = 30.dp,
                    side = 15.dp,
                    gridTypeList = gridTypeList
                )
            )
            rvContents.setSpanSpecialGridType(gridTypeList, 2)
            rvContents.adapter = adapter
        }

        handleDummy()
    }

    private fun handleDummy() {
        lifecycleScope.launch(Dispatchers.Main) {
            dummyList.clear()
            dummyList.addAll(getDummyList())
            adapter.submitList(dummyList)
            delay(3_000)
            dummyList.removeLast()
            dummyList.removeLast()
            dummyList.removeLast()
            dummyList.removeLast()
            val tmpDummyList = getDummyList()
            val each = dummyList.iterator()
            while (each.hasNext()) {
                if (each.next().layoutId == R.layout.vh_bottom_space) {
                    each.remove()
                }
            }
            dummyList.addAll(tmpDummyList)
            adapter.submitList(dummyList)
        }
    }

    private fun getDummyList(): List<SpecialItem> {
        val list = mutableListOf<SpecialItem>()
        val ranSize = Random.nextInt(20, 21)
        for (idx in 0 until ranSize) {
            if (idx < 5) {
                list.add(
                    if (Random.nextBoolean()) {
                        SpecialItem(R.layout.vh_special_linear_1)
                    } else {
                        SpecialItem(R.layout.vh_special_linear_2)
                    }
                )
            } else if (idx in 16..19)
                list.add(
                    if (Random.nextBoolean()) {
                        SpecialItem(R.layout.vh_special_linear_1)
                    } else {
                        SpecialItem(R.layout.vh_special_linear_2)
                    }
                )
            else {
                list.add(SpecialItem(R.layout.vh_special_grid_1))
            }
        }
        // Last BottomSpace
        list.add(SpecialItem(R.layout.vh_bottom_space))
        return list
    }

    class Adapter : RecyclerView.Adapter<BaseSpecialViewHolder>() {

        private val dataList = mutableListOf<SpecialItem>()

        fun submitList(newList: List<SpecialItem>) {
            val prevCount = dataList.size
            dataList.clear()
            dataList.addAll(newList)
            notifyItemRangeChanged(prevCount, newList.size)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseSpecialViewHolder {
            return when (viewType) {
                R.layout.vh_special_linear_1 -> Linear1ViewHolder(parent)
                R.layout.vh_special_linear_2 -> Linear2ViewHolder(parent)
                R.layout.vh_bottom_space -> SpaceViewHolder(parent)
                else -> Grid1ViewHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: BaseSpecialViewHolder, pos: Int) {
            if (dataList.size > pos) {
                runCatching {
                    holder.onBindView(dataList[pos])
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

        inner class Linear1ViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder(
                parent,
                R.layout.vh_special_linear_1
            ) {

            private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }

            override fun onBindView(item: Any) {
                if (item is SpecialItem) {
                    tvTitle.text = "$bindingAdapterPosition"
                }
            }
        }

        inner class Linear2ViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder(
                parent,
                R.layout.vh_special_linear_2
            ) {

            private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }

            override fun onBindView(item: Any) {
                if (item is SpecialItem) {
                    tvTitle.text = "$bindingAdapterPosition"
                }
            }
        }

        inner class Grid1ViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder(parent, R.layout.vh_special_grid_1) {

            private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }

            override fun onBindView(item: Any) {
                if (item is SpecialItem) {
                    tvTitle.text = "$bindingAdapterPosition"
                }
            }
        }

        inner class SpaceViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder(parent, R.layout.vh_bottom_space) {
            override fun onBindView(item: Any) {
            }
        }
    }

    abstract class BaseSpecialViewHolder(
        parent: ViewGroup,
        @LayoutRes layoutId: Int
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    ) {

        @Throws(Exception::class)
        abstract fun onBindView(item: Any)
    }
}
