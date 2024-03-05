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
    private var halfHeaderHeight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.a_dynamic_coordinator)
        binding.tvContents.post { dynamicContentsHeight = binding.tvContents.height }
        binding.llHeader.post {
            headerHeight = binding.llHeader.height
            halfHeaderHeight = (headerHeight.toFloat() / 2).toInt()
        }
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
                    val newOffset = getAdjustRangeValue(
                        standardValue = offset,
                        standardEnd = dynamicContentsHeight,
                        targetEnd = halfHeaderHeight
                    )
                    binding.llHeader.translationY = -newOffset
                } else {
                    if (currentState == State.COLLAPSED &&
                        offset in totalRange.minus(halfHeaderHeight)..totalRange
                    ) {
                        val newOffset = getAdjustRangeValue(
                            standardValue = offset,
                            standardStart = totalRange.minus(halfHeaderHeight),
                            standardEnd = totalRange,
                            targetEnd = -halfHeaderHeight
                        )
                        binding.llHeader.translationY = newOffset
                    }
                }
            }
        })
    }

    /**
     * 기준이 되는 범위, 위치와 값에 따라서
     * 새로운 범위에서 위치값을 리턴하는 함수
     * @param standardValue 기준이 되는 위치값
     * @param standardStart 기준이 되는 시작
     * @param standardEnd 기준이 되는 끝
     * @param targetStart 새로운 범위의 시작
     * @param targetEnd 새로운 범위의 끝
     */
    fun getAdjustRangeValue(
        standardValue: Int,
        standardStart: Int = 0,
        standardEnd: Int,
        targetStart: Int = 0,
        targetEnd: Int
    ): Float {
        val standardStartF = standardStart.toFloat()
        val standardEndF = standardEnd.toFloat()
        val targetStartF = targetStart.toFloat()
        val targetEndF = targetEnd.toFloat()
        val ratio = (standardValue.minus(standardStartF)) / standardEndF.minus(standardStartF)
        return ratio * (targetEndF.minus(targetStartF)) + targetStartF
    }
}
