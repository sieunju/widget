package com.hmju.visual.ui.tablayout

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hmju.visual.R
import hmju.widget.tablayout.LinePagerTabLayout
import hmju.widget.tablayout.PagerTabItem

/**
 * Description :
 *
 * Created by juhongmin on 2022/01/07
 */
internal class CustomTabLayoutFragment : Fragment(R.layout.fragment_custom_tab_layout) {

    private lateinit var tbScrollable: LinePagerTabLayout
    private lateinit var tbFixed: LinePagerTabLayout
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            tbScrollable = findViewById(R.id.tbScrollable)
            tbFixed = findViewById(R.id.tbFixed)
            viewPager = findViewById(R.id.vp)

            tbScrollable.viewPager = viewPager
            tbScrollable.setDataList(getTabDataList())
            tbFixed.viewPager = viewPager
            tbFixed.setDataList(getTabDataList())

            viewPager.adapter = Adapter(getViewPagerList())
        }
    }

    private fun getTabDataList(): List<PagerTabItem> {
        val list = mutableListOf<PagerTabItem>()
        list.add(PagerTabItem("OneTab"))
        list.add(PagerTabItem("Two....Tab"))
        list.add(PagerTabItem("ThreeTab"))
        list.add(PagerTabItem("Four"))
        return list
    }

    private fun getViewPagerList(): List<Int> {
        val list = mutableListOf<Int>()
        list.add(Color.GRAY)
        list.add(Color.BLACK)
        list.add(Color.RED)
        list.add(Color.CYAN)
        return list
    }

    class Adapter(private val data: List<Int>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            holder.bindView(data[pos])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.vh_sample_custom_tab_layout, parent, false
            )
        ) {
            private val view = itemView.findViewById<View>(R.id.v)

            fun bindView(@ColorInt color: Int) {
                view.setBackgroundColor(color)
            }
        }
    }
}