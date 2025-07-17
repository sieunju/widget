package com.hmju.visual.ui.recyclerview

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hmju.visual.R
import hmju.widget.view.WalletStackView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        })
        wallet.setSpanStackHeight(20.dp.toFloat())
        wallet.setItems(dataList)
        lifecycleScope.launch {
            delay(500)
            wallet.startAni()
        }
    }
}