package com.hmju.visual.ui.viewpager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hmju.visual.R
import hmju.widget.viewpager.indicator.DotIndicator
import hmju.widget.viewpager.indicator.LineIndicator
import hmju.widget.viewpager.scroller.AutoScrollMediator

/**
 * Description :
 *
 * Created by juhongmin on 2022/01/19
 */
internal class ViewPagerFragment : Fragment(R.layout.f_viewpager) {

    private lateinit var fillIndicator: LineIndicator
    private lateinit var unitIndicator: LineIndicator
    private lateinit var dotIndicator: DotIndicator
    private lateinit var vp: ViewPager2

    data class Sample(val pos: Int, val color: Int)

    private lateinit var autoScrollMediator: AutoScrollMediator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            fillIndicator = findViewById(R.id.fillIndicator)
            unitIndicator = findViewById(R.id.unitIndicator)
            dotIndicator = findViewById(R.id.dotIndicator)
            vp = findViewById(R.id.vp)

            unitIndicator.viewPager = vp
            fillIndicator.viewPager = vp
            dotIndicator.viewPager = vp

            autoScrollMediator = AutoScrollMediator(vp, 1000L)

            val list = getViewPagerDataList()
            vp.adapter = Adapter(vp, list).apply {
                updateCurrentPos()
            }
            vp.offscreenPageLimit = list.size

        }
    }

    override fun onResume() {
        super.onResume()
        autoScrollMediator.startAutoScroll()
    }

    override fun onStop() {
        super.onStop()
        autoScrollMediator.stopAutoScroll()
    }

    private fun getViewPagerDataList(): List<Sample> {
        val list = mutableListOf<Sample>()
        list.add(Sample(0, Color.BLACK))
        list.add(Sample(1, Color.DKGRAY))
        list.add(Sample(2, Color.GRAY))
        list.add(Sample(3, Color.LTGRAY))
        list.add(Sample(4, Color.WHITE))
        list.add(Sample(5, Color.RED))
        list.add(Sample(6, Color.GREEN))
        list.add(Sample(7, Color.BLUE))
        list.add(Sample(8, Color.YELLOW))
        list.add(Sample(9, Color.CYAN))
        list.add(Sample(10, Color.MAGENTA))
        return list
    }

    class Adapter(
        private val viewPager: ViewPager2,
        private val list: List<Sample>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var isInfinite: Boolean = true
        private var currentPos: Int = 0

        private val pageListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPos = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (isInfinite && (state == ViewPager2.SCROLL_STATE_IDLE ||
                            state == ViewPager2.SCROLL_STATE_DRAGGING)
                ) {

                    // Fake Last Item
                    if (currentPos == 0) {
                        // Move Real Last Item
                        viewPager.post {
                            viewPager.currentItem(itemCount - 2, false)
                        }
                    } else if (currentPos == itemCount - 1) {
                        // Fake First Item.
                        // Move First Item
                        viewPager.post {
                            viewPager.currentItem(1, false)
                        }
                    }
                }
            }
        }

        init {
            viewPager.unregisterOnPageChangeCallback(pageListener)
            viewPager.registerOnPageChangeCallback(pageListener)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            getData(pos, list)?.let {
                holder.bindView(it)
            }
        }

        override fun getItemCount(): Int {
            return if (isInfinite) list.size.plus(2) else list.size
        }

        fun updateCurrentPos() {
            viewPager.post {
                if (isInfinite) {
                    if (currentPos == 0) {
                        currentPos = 1
                    }
                }
                viewPager.currentItem(currentPos, false)
            }
        }

        private fun getData(pos: Int, list: List<Sample>): Sample? {
            var index = pos
            if (isInfinite) {
                index = when (pos) {
                    0 -> {
                        // Fake LastIndex Item
                        list.lastIndex
                    }
                    itemCount - 1 -> {
                        // Fake FirstIndex Item
                        0
                    }
                    else -> {
                        // Other
                        pos - 1
                    }
                }
            }
            return if (list.size > index) {
                list[index]
            } else {
                null
            }
        }

        inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.vh_sample_custom_tab_layout, parent, false
            )
        ) {
            private val view = itemView.findViewById<View>(R.id.v)
            private val tvTitle = itemView.findViewById<AppCompatTextView>(R.id.tvTitle)

            fun bindView(item: Sample) {
                view.setBackgroundColor(item.color)
                tvTitle.text = "POS ${item.pos}"
            }
        }

        fun ViewPager2.currentItem(pos: Int, smoothScroll: Boolean = true) {
            if (isFakeDragging) {
                endFakeDrag()
            }
            setCurrentItem(pos, smoothScroll)
        }
    }
}