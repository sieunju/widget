package com.hmju.visual.ui.coordinator

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.AppBarLayout
import com.hmju.visual.R
import com.hmju.visual.databinding.ADynamicCoordinatorBinding
import kotlin.math.abs


/**
 * Description :
 *
 * Created by juhongmin on 3/3/24
 */
internal class DynamicCoordinatorActivity : AppCompatActivity() {

    val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    enum class State {
        EXPANDED,
        COLLAPSED
    }

    private lateinit var binding: ADynamicCoordinatorBinding
    private var dynamicContentsHeight = -1
    private var headerHeight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_dynamic_coordinator)
        binding.tvContents.post { dynamicContentsHeight = binding.tvContents.height }
        binding.llHeader.post { headerHeight = binding.llHeader.height }
        binding.abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var currentState: State? = null

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange
                val offset = abs(verticalOffset)
                if (offset == 0) {
                    currentState = State.EXPANDED
                } else if (offset >= totalRange) {
                    currentState = State.COLLAPSED
                }
                if (dynamicContentsHeight == -1 || headerHeight == -1) return

                if (offset <= dynamicContentsHeight && currentState == State.EXPANDED) {
                    val ratioY = getExpandRatioY(
                        offset,
                        0,
                        dynamicContentsHeight,
                        0,
                        (headerHeight.toFloat() / 2).toInt()
                    )
                    binding.llHeader.translationY = -ratioY
                } else {
                    if (currentState == State.COLLAPSED && offset in totalRange.minus(headerHeight / 2)..totalRange) {
                        val ratioY = getCollapsedRatioY(offset, totalRange)
                        binding.llHeader.translationY = ratioY
                    }
                }
            }
        })
    }

    fun getCollapsedRatioY(
        value: Int,
        totalRange: Int
    ): Float {
        // 비율 계산
        // 접힌 상태에서 펼쳐졌을때와 접힐떄 위치, S:200 E:250
        // 600 ~ 750
        val ratio =
            (value.toFloat() - totalRange.minus(headerHeight / 2)) / (headerHeight / 2).toFloat()
        // 헤더 이동 S:0, E: -50
        return ratio * -(headerHeight / 2)
    }

    fun getExpandRatioY(
        value: Int,
        min1: Int,
        max1: Int,
        min2: Int,
        max2: Int
    ): Float {
        val ratio = (value.toFloat() - min1.toFloat()) / (max1 - min1).toFloat()
        return ratio * (max2 - min2) + min2
    }
}
