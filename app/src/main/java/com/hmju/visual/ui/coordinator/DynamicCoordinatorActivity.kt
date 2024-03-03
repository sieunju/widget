package com.hmju.visual.ui.coordinator

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_dynamic_coordinator)
        val llHeader = findViewById<LinearLayoutCompat>(R.id.llHeader)
        findViewById<AppBarLayout>(R.id.abl).addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalRange = appBarLayout.totalScrollRange
            val offset = Math.abs(verticalOffset)
            Timber.d("Scroll Offset $totalRange $offset")

            val diffOffset = if (offset <= 50.dp) {
                -offset
            } else {
                val aOffset = Math.min(50.dp, totalRange.minus(offset))
                val bOffset = 50.dp.minus(aOffset)
                -50.dp
            }
            llHeader.translationY = diffOffset.toFloat()
        }

    }
}
