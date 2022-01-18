package com.hmju.visual

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import hmju.widget.tablayout.LinePagerTabLayout
import hmju.widget.tablayout.PagerTabItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Description :
 *
 * Created by juhongmin on 2022/01/07
 */
class CustomTabLayoutFragment : Fragment(R.layout.fragment_custom_tab_layout) {

    private val colorArr = arrayOf(
        Color.BLACK, Color.WHITE,
        Color.BLUE, Color.RED,
        Color.GREEN, Color.CYAN
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val tabLayout = findViewById<LinePagerTabLayout>(R.id.tabLayout)
            val viewPager = findViewById<ViewPager2>(R.id.vp)

            tabLayout.viewPager = viewPager

            GlobalScope.launch(Dispatchers.Main) {
                val tabList = mutableListOf<PagerTabItem>()
                val colorList = mutableListOf<Int>()
                tabList.add(PagerTabItem("TAB1"))
                tabList.add(PagerTabItem("TAB2"))
                tabList.add(PagerTabItem("TAB3"))
                tabList.add(PagerTabItem("TAB4"))

                colorList.add(Color.WHITE)
                colorList.add(Color.WHITE)
                colorList.add(Color.WHITE)
                colorList.add(Color.WHITE)

                viewPager.adapter = Adapter(colorList)
                tabLayout.setDataList(tabList)
            }
        }
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