package com.hmju.visual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hmju.visual.databinding.*
import hmju.widget.extensions.dp
import hmju.widget.layoutmanager.SpecialDecorationBindingAdapter.setSpanSpecialGridType
import hmju.widget.layoutmanager.SpecialGridItemDecoration
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Description :
 *
 * Created by juhongmin on 2022/03/21
 */
class SpecialGridFragment : Fragment() {

    companion object {
        class SimpleDiffUtil(
            private val oldList: List<SpecialItem>,
            private val newList: List<SpecialItem>
        ) : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                val oldItem = oldList[oldPos]
                val newItem = newList[newPos]
                return oldItem.msg == newItem.msg
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val oldItem = oldList[oldPos]
                val newItem = newList[newPos]
                return oldItem.msg == newItem.msg
            }
        }
    }

    data class SpecialItem(
        val msg: String,
        @LayoutRes val layoutId: Int
    )

    private val dummyAdapter: Adapter by lazy { Adapter() }

    private lateinit var binding: FSpecialGridBinding
    private val gridTypeList = listOf(R.layout.vh_special_grid_1)
    private val dummyList = mutableListOf<SpecialItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FSpecialGridBinding>(
            inflater,
            R.layout.f_special_grid,
            container,
            false
        ).run {
            binding = this
            this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gridTypeList = gridTypeList
        // binding.rvContents.setSpanSpecialGridType(gridTypeList)
        binding.rvContents.addItemDecoration(
            SpecialGridItemDecoration(
                horizontalDivider = 20.dp,
                verticalDivider = 60.dp,
                side = 15.dp,
                gridTypeList = gridTypeList
            )
        )
        GlobalScope.launch(Dispatchers.Main) {
            binding.rvContents.adapter = dummyAdapter
            dummyList.addAll(getDummyList())
            dummyAdapter.setData(dummyList)
            delay(5_000)
            dummyList.removeLast()
            val tmpDummyList = getDummyList(dummyList.size.plus(1))
            val each = dummyList.iterator()
            while (each.hasNext()) {
                if (each.next().layoutId == R.layout.vh_bottom_space) {
                    each.remove()
                }
            }
            dummyList.addAll(tmpDummyList)
            dummyAdapter.setData(dummyList)
        }
    }

    private suspend fun getDummyList(startIdx: Int = 0): List<SpecialItem> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<SpecialItem>()
            val ranSize = Random.nextInt(60, 61)
            for (idx in 0 until ranSize) {
                val tmpIdx = startIdx.plus(idx)
                if (idx < 10) {
                    list.add(
                        if (Random.nextBoolean()) {
                            SpecialItem(
                                "POS_$tmpIdx",
                                R.layout.vh_special_linear_1
                            )
                        } else {
                            SpecialItem(
                                "POS_$tmpIdx",
                                R.layout.vh_special_linear_2
                            )
                        }
                    )
                } else if (idx in 16..19)
                    list.add(
                        if (Random.nextBoolean()) {
                            SpecialItem(
                                "POS_$tmpIdx",
                                R.layout.vh_special_linear_1
                            )
                        } else {
                            SpecialItem(
                                "POS_$tmpIdx",
                                R.layout.vh_special_linear_2
                            )
                        }
                    )
                else {
                    list.add(
                        SpecialItem(
                            "POS_$tmpIdx",
                            R.layout.vh_special_grid_1
                        )
                    )
                }
            }
            // Last BottomSpace
            list.add(
                SpecialItem(
                    msg = "LAST",
                    R.layout.vh_bottom_space
                )
            )
            list
        }
    }

    class Adapter : RecyclerView.Adapter<BaseSpecialViewHolder<*>>() {

        private val dataList = mutableListOf<SpecialItem>()

        fun setData(newList: List<SpecialItem>) {
            val diffResult = DiffUtil.calculateDiff(SimpleDiffUtil(dataList, newList))
            dataList.clear()
            dataList.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseSpecialViewHolder<*> {
            return when (viewType) {
                R.layout.vh_special_linear_1 -> Linear1ViewHolder(parent)
                R.layout.vh_special_linear_2 -> Linear2ViewHolder(parent)
                R.layout.vh_bottom_space -> SpaceViewHolder(parent)
                else -> Grid1ViewHolder(parent)
            }
        }

        override fun onBindViewHolder(holder: BaseSpecialViewHolder<*>, pos: Int) {
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
            BaseSpecialViewHolder<VhSpecialLinear1Binding>(
                parent,
                R.layout.vh_special_linear_1
            ) {

            override fun onBindView(item: Any) {
                binding.setVariable(BR.pos, "Pos $adapterPosition")
                binding.setVariable(BR.model, item)
            }
        }

        inner class Linear2ViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder<VhSpecialLinear2Binding>(
                parent,
                R.layout.vh_special_linear_2
            ) {

            override fun onBindView(item: Any) {
                binding.setVariable(BR.pos, "Pos $adapterPosition")
                binding.setVariable(BR.model, item)
            }
        }

        inner class Grid1ViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder<VhSpecialGrid1Binding>(
                parent,
                R.layout.vh_special_grid_1
            ) {
            override fun onBindView(item: Any) {
                binding.setVariable(BR.pos, "Pos $adapterPosition")
                binding.setVariable(BR.model, item)
            }
        }

        inner class SpaceViewHolder(parent: ViewGroup) :
            BaseSpecialViewHolder<VhBottomSpaceBinding>(
                parent,
                R.layout.vh_bottom_space
            ) {
            override fun onBindView(item: Any) {
                binding.setVariable(BR.model, item)
            }
        }
    }

    abstract class BaseSpecialViewHolder<T : ViewDataBinding>(
        parent: ViewGroup,
        @LayoutRes layoutId: Int
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    ) {

        val binding: T by lazy { DataBindingUtil.bind(itemView)!! }

        @Throws(Exception::class)
        abstract fun onBindView(item: Any)
    }
}