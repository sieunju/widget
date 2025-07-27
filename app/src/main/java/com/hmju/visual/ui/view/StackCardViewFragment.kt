package com.hmju.visual.ui.view

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.hmju.visual.Constants
import com.hmju.visual.ImageLoader
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
        val imageUrl: String = Constants.LogoThumb.LOGO_PURPLE
    )

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    private lateinit var requestManager: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager = Glide.with(this)
        val wallet = view.findViewById<WalletStackView<Card>>(R.id.vWallet)
            .setSpanStackHeight(20f.dp)
            .setStartTranslationY(150f.dp)
            .setStackCount(3)
        val dataList = mutableListOf<Card>()
        dataList.add(Card("Index 0"))
        dataList.add(Card("Index 1"))
        dataList.add(Card("Index 2"))
        wallet.setListener(object : WalletStackView.Listener<Card> {

            override fun initView(item: Card, parent: ViewGroup): View {
                val childView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.v_wallet_card, parent, false)
                childView.findViewById<AppCompatTextView>(R.id.tvTitle).text = item.title
                val ivThumb = childView.findViewById<AppCompatImageView>(R.id.ivThumb)
                requestManager.load(item.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(ivThumb)
                return childView
            }

            override fun onItemClick(item: Card) {
                Snackbar.make(view, "Click ${item.title}", Snackbar.LENGTH_SHORT).show()
            }

            override fun onStartAniCompleted() {
                Snackbar.make(view, "onStartAniCompleted", Snackbar.LENGTH_SHORT).show()
            }
        })
        wallet.setItems(dataList)
        wallet.startAni()
    }
}