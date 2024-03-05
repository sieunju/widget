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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_dynamic_coordinator)
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

                // Timber.d("ScrollOffset $offset")
                if (offset <= totalRange.minus(100.dp) && currentState == State.EXPANDED) {
                    val ratioY = getExpandRatioY(offset, 0, totalRange.minus(100.dp), 0, 50.dp)
                    binding.llHeader.translationY = -ratioY
                } else {
                    if (currentState == State.COLLAPSED && offset in totalRange.minus(50.dp)..totalRange) {
                        val ratioY = getCollapsedRatioY(offset)
                        binding.llHeader.translationY = ratioY
                    }
                }
            }
        })
    }

    fun getCollapsedRatioY(value: Int): Float {
        if (value !in 200.dp..250.dp) {
            return (-50F).dp
        }
        // 비율 계산
        // 접힌 상태에서 펼쳐졌을때와 접힐떄 위치, S:200 E:250
        val ratio = (value.toFloat() - 200F.dp) / (50F.dp)
        // 헤더 이동 S:0, E: -50
        return ratio * (-50F).dp
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
