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
        val tbHeader = findViewById<Toolbar>(R.id.tbHeader)
        val tbFilter = findViewById<Toolbar>(R.id.tbFilter)
        val abl = findViewById<AppBarLayout>(R.id.abl)
        val statusBarHeight = getStatusBarHeight()
        abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var currentState: State? = null
            var currentOffset = 0
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange
                val offset = Math.abs(verticalOffset)
                val tbLocation = IntArray(2)
                tbFilter.getLocationOnScreen(tbLocation)
                tbLocation[1] = tbLocation[1].minus(statusBarHeight) // max 900, 필터 영역 까지 Top. 300.dp
                val isGestureUp = (offset - currentOffset) > 0
//                if (offset == 0) {
//                    currentState = State.EXPANDED
//                } else if (offset >= appBarLayout.totalScrollRange && currentState == State.EXPANDED) {
//                    currentState = State.COLLAPSED
//                }
                // Timber.d("Scroll Offset $offset $isGestureUp ${tbLocation[1]}")
                val diffOffset = if (offset <= 50.dp) {
                    -offset
                }
//                else if (offset in totalRange.minus(50.dp)..totalRange && !isGestureUp) {
//                    val downOffset = offset - totalRange.minus(50.dp)
//                    // Timber.d("아래 제스쳐 Offset $downOffset")
//                    -downOffset
//                } else if (offset in 51.dp until totalRange.minus(50.dp) && !isGestureUp) {
//                    Timber.d("Gesture Up")
//                    0
//                } else {
//                    Timber.d("여기를 탑니까? ")
//                    null
//                    // (-50).dp
//                }
                else {
                    val aOffset = totalRange.minus(tbLocation[1])
                    Timber.d("ScrollOffset ${Math.min(50.dp,aOffset)} ${aOffset}")
                    // -Math.min(tbLocation[1].minus(50.dp),50.dp)
                     -50.dp
                }
                llHeader.translationY = diffOffset.toFloat()
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
