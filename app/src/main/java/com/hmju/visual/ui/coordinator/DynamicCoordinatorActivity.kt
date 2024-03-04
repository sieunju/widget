package com.hmju.visual.ui.coordinator

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.AppBarLayout
import com.hmju.visual.R
import com.hmju.visual.databinding.ADynamicCoordinatorBinding
import timber.log.Timber
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

    enum class State {
        EXPANDED,
        COLLAPSED,
        HALF_COLLAPSED
    }

    private lateinit var binding: ADynamicCoordinatorBinding

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.a_dynamic_coordinator)
        val statusBarHeight = getStatusBarHeight()
        Timber.d("StatusBar $statusBarHeight")
        binding.abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var currentState: State? = null
            var currentOffset = 0
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange // 750
                val offset = Math.abs(verticalOffset)
                val appBarTop = appBarLayout.top
                val appBarBottom = appBarLayout.bottom
                val appBarHeight = if (appBarTop > appBarBottom) {
                    appBarTop.plus(appBarBottom)
                } else {
                    appBarBottom.plus(appBarTop)
                }

                if (offset == 0) {
                    currentState = State.EXPANDED
                } else if (offset >= totalRange) {
                    currentState = State.COLLAPSED
                }

                // 아직 안걸쳐짐
                if (appBarHeight > (-50).dp) {
                    binding.llHeader.translationY = -(Math.min(50.dp,offset)).toFloat()
                    Timber.d("아직 안걸쳐짐 ${appBarHeight} $offset ${binding.llHeader.translationY}")
                } else {
                    // 걸쳐진 상태
                    // -150.dp ~ -50.dp
                    if (currentState == State.COLLAPSED) {
                        val newValue = adjustRange(abs(appBarHeight), 50.dp,150.dp,0,50.dp)
                        binding.llHeader.translationY = -newValue
                        Timber.d("걸쳐진 상태 $appBarHeight $newValue  ${binding.nsv.top}")
                    }
                }
            }
        })
    }

    fun adjustRange(value: Int, min1: Int, max1: Int, min2: Int, max2: Int): Float {
        // 현재 값의 비율 계산
        val ratio = (value.toFloat() - min1.toFloat()) / (max1.toFloat() - min1.toFloat())
        return ratio * (max2 - min2) + min2
    }

    @SuppressLint("InternalInsetResource")
    fun getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}
