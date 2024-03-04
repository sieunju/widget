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
import kotlin.math.min


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

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_dynamic_coordinator)
        // val llHeader = findViewById<LinearLayoutCompat>(R.id.llHeader)
//        val tbCustom = findViewById<CustomCollapsingToolbarLayout>(R.id.tbCustom)
//        val tbHeader = findViewById<Toolbar>(R.id.tbHeader)
//        val llContents = findViewById<LinearLayoutCompat>(R.id.llContents)
//        val tbTop = findViewById<Toolbar>(R.id.tbTop)
//        val tbBottom = findViewById<Toolbar>(R.id.tbBottom)
//        val tbCustom = findViewById<CustomCollapsingToolbarLayout>(R.id.tbCustom)
        val tbCategory = findViewById<Toolbar>(R.id.tbCategory)
        val abl = findViewById<AppBarLayout>(R.id.abl)
        val statusBarHeight = getStatusBarHeight()
        Timber.d("StatusBar $statusBarHeight")
        abl.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var currentState: State? = null
            var currentOffset = 0
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val totalRange = appBarLayout.totalScrollRange // 750
                val offset = Math.abs(verticalOffset)
                // tbCategory.translationY = -Math.min(50.dp,offset.minus(100.dp)).toFloat()
                Timber.d("ScrollOffset $offset")
                if (offset <= 50.dp) {
                    tbCategory.translationY = -Math.max(offset, 0).toFloat()
                } else if (offset in 50.dp .. totalRange.minus(50.dp)) {
                    tbCategory.translationY = -50.dp.toFloat()
                } else if (offset in totalRange.minus(50.dp)..totalRange) {
                    tbCategory.translationY = -totalRange.minus(offset).toFloat()
                }
                currentOffset = offset
            }
        })
    }

    @SuppressLint("InternalInsetResource")
    fun getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}
