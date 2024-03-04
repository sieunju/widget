package com.hmju.visual.ui.coordinator

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.hmju.visual.R
import timber.log.Timber


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
        COLLAPSED
    }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_dynamic_coordinator)
        val llHeader = findViewById<LinearLayoutCompat>(R.id.llHeader)
        val tbCustomTest = findViewById<CustomCollapsingToolbarLayout>(R.id.tbCustomTest)
        val tbTest = findViewById<Toolbar>(R.id.tvTest)
        val tbFilter = findViewById<Toolbar>(R.id.tbFilter)
        val abl = findViewById<AppBarLayout>(R.id.abl)
        val statusBarHeight = getStatusBarHeight()
        abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var currentState: State? = null
            var currentOffset = 0
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange
                val offset = Math.abs(verticalOffset)
                if (offset == 0) {
                    currentState = State.EXPANDED
                } else if (offset >= totalRange) {
                    currentState = State.COLLAPSED
                }
                val tbLocation = IntArray(2)
                val tbTestLocation = IntArray(2)
                tbTest.getLocationOnScreen(tbTestLocation)
                tbTestLocation[1] = tbTestLocation[1].minus(statusBarHeight)
                // Timber.d("1111 ${tbCustomTest.measuredHeight}")
                val aOffset = Math.min(tbTestLocation[1],50.dp)
                Timber.d("호호 ${aOffset}")
                // llHeader.translationY = aOffset.minus(50.dp).toFloat()
                tbFilter.getLocationOnScreen(tbLocation)
                tbLocation[1] = tbLocation[1].minus(statusBarHeight).minus(50.dp) // max 900, 필터 영역 까지 Top. 300.dp
                val isGestureUp = (offset - currentOffset) > 0
                // Timber.d("ScrollOffset ${tbLocation[1]}")
                val diffOffset = if (tbLocation[1] in 0..50.dp && currentState == State.COLLAPSED) {
                    // 150 .. 300
                    // return -150 .. 0
                    tbLocation[1].minus(50.dp)
                } else {
                    val aOffset = tbLocation[1].minus(totalRange).minus(50.dp)
                    val bOffset = Math.max(aOffset,-50.dp)
                    // Timber.d("호호호 $bOffset")
                    bOffset
                }
                // llHeader.translationY = diffOffset.toFloat()
                currentOffset = offset
            }
        })
    }

    @SuppressLint("InternalInsetResource")
    fun getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else -1
    }
}
