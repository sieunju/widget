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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Description :
 *
 * Created by juhongmin on 2022/01/07
 */
internal class CustomTabLayoutFragment : Fragment(R.layout.fragment_custom_tab_layout) {

    private val colorArr = arrayOf(
        Color.BLACK, Color.WHITE,
        Color.BLUE, Color.RED,
        Color.GREEN, Color.CYAN
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val tabLayout = findViewById<hmju.widget.tablayout.LinePagerTabLayout>(R.id.tabLayout)
            val viewPager = findViewById<ViewPager2>(R.id.vp)

            tabLayout.viewPager = viewPager
            tabLayout.addObserver(this@CustomTabLayoutFragment)

            GlobalScope.launch(Dispatchers.Main) {
                val tabList = mutableListOf<hmju.widget.tablayout.PagerTabItem>()
                val colorList = mutableListOf<Int>()
                tabList.add(hmju.widget.tablayout.PagerTabItem("oneTab"))
                tabList.add(hmju.widget.tablayout.PagerTabItem("TwoTwoTab"))
                tabList.add(hmju.widget.tablayout.PagerTabItem("ThreeTab"))
                tabList.add(hmju.widget.tablayout.PagerTabItem("FourTab"))

                colorList.add(Color.WHITE)
                colorList.add(Color.BLACK)
                colorList.add(Color.RED)
                colorList.add(Color.CYAN)

                viewPager.adapter = Adapter(colorList)
                tabLayout.setDataList(tabList)
            }
            GlobalScope.launch(Dispatchers.Main) {
                delay(5000)
                viewPager.setCurrentItem(2,true)
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