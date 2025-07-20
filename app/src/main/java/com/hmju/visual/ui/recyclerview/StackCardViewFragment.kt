package com.hmju.visual.ui.recyclerview

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hmju.visual.R
import hmju.widget.extensions.Extensions.dp
import hmju.widget.view.WalletStackView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Description :
 *
 * Created by juhongmin on 2025. 7. 16.
 */
internal class StackCardViewFragment : Fragment(R.layout.f_stack_card) {

    data class Card(
        val title: String,
        val color: Int
    )

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wallet = view.findViewById<WalletStackView<Card>>(R.id.vWallet)
            .setCardHeight(160.dp)
            .setStackCount(3)
        val dataList = mutableListOf<Card>()
        dataList.add(Card("Index 0", Color.DKGRAY))
        dataList.add(Card("Index 1", Color.DKGRAY))
        dataList.add(Card("Index 2", Color.DKGRAY))
        dataList.add(Card("Index 3", Color.DKGRAY))
        wallet.setListener(object : WalletStackView.Listener<Card> {

            override fun initView(item: Card, parent: ViewGroup): View {
                val childView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.v_wallet_card, parent, false)
                childView.findViewById<AppCompatTextView>(R.id.tvTitle).text = item.title
                if (childView is CardView) {
                    childView.setCardBackgroundColor(item.color)
                }

                return childView
            }

            override fun onClickEvent(item: Card) {
                Timber.d("onClickEvent")
            }
        })
        wallet.setSpanStackHeight(20.dp.toFloat())
        wallet.setItems(dataList)
        lifecycleScope.launch {
            delay(500)
            wallet.startAni()
        }

        val rvSelector = view.findViewById<RecyclerView>(R.id.rvSelector)
        rvSelector.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvSelector.adapter = ExampleAdapter()
        AdjustableLinearSnapHelper(view.context).attachToRecyclerView(rvSelector)
        rvSelector.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var currentPosition = 0
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val findFirstPosition = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (findFirstPosition != currentPosition) {
                    rv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    currentPosition = findFirstPosition
                }
            }
        })
    }

    class AdjustableLinearSnapHelper(
        private val context: Context,
        private val speedMultiplier: Float = 1.0f,
        private val velocityThreshold: Int = 1000
    ) : LinearSnapHelper() {

        override fun createScroller(layoutManager: RecyclerView.LayoutManager): RecyclerView.SmoothScroller {
            return object : LinearSmoothScroller(context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return (25f * speedMultiplier) / displayMetrics.densityDpi
                }

                override fun calculateTimeForDeceleration(dx: Int): Int {
                    return (super.calculateTimeForDeceleration(dx) * speedMultiplier).toInt()
                        .coerceAtMost(300)
                }
            }
        }

        override fun findTargetSnapPosition(
            layoutManager: RecyclerView.LayoutManager?,
            velocityX: Int,
            velocityY: Int
        ): Int {
            // 속도 임계값으로 민감도 조절
            val velocity = if (layoutManager?.canScrollHorizontally() == true) velocityX else velocityY

            return if (Math.abs(velocity) < velocityThreshold) {
                // 느린 스크롤은 가장 가까운 아이템에 스냅
                layoutManager?.let { lm ->
                    val snapView = findSnapView(lm)
                    snapView?.let { lm.getPosition(it) } ?: RecyclerView.NO_POSITION
                } ?: RecyclerView.NO_POSITION
            } else {
                super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
            }
        }
    }


    private class ExampleAdapter : RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder>() {

        private val dataList = listOf<Int>(
            Color.DKGRAY,
            Color.RED,
            Color.LTGRAY,
            Color.BLUE,
            Color.MAGENTA,
            Color.YELLOW,
            Color.DKGRAY,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
            val view = CardView(parent.context)
            view.radius = 30.dp.toFloat()
            view.setCardBackgroundColor(viewType)
            view.layoutParams = ConstraintLayout.LayoutParams(150.dp, 150.dp).apply {
                leftMargin = 15.dp
                rightMargin = 15.dp
            }
            view.elevation = 8.dp.toFloat()
            return ExampleViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {

        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return dataList[position]
        }

        class ExampleViewHolder(
            val cardView: CardView
        ) : RecyclerView.ViewHolder(cardView)
    }
}