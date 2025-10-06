package com.hmju.visual.ui.select

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hmju.visual.MainActivity.Companion.moveToFragment
import com.hmju.visual.R
import com.hmju.visual.ui.select.models.Card
import com.hmju.visual.ui.select.models.Card.Companion.getThumbContents
import com.hmju.visual.ui.select.models.SelectState
import com.hmju.visual.ui.select.repository.GithubRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * Description : Visual Ui Selection Menu Fragment
 *
 * Created by juhongmin on 2022/12/30
 */
internal class SelectMenuFragment : Fragment(R.layout.f_select_menu) {

    private lateinit var useCase: SelectionUseCase

    data class MenuUiModel(
        val title: String,
        val data: Card
    ) {
        constructor(data: Card) : this(
            title = when (data) {
                is Card.FragmentCard -> data.title
                is Card.ActivityCard -> data.title
            },
            data = data
        )
    }

    private lateinit var rvContents: RecyclerView

    private val adapter: Adapter by lazy { Adapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useCase = SelectionUseCase(GithubRepository(view.context.applicationContext))
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = GridLayoutManager(view.context, 2)
        rvContents.adapter = adapter

        useCase().onEach {
            Timber.d("State:${Thread.currentThread()}")
            when (it) {
                is SelectState.Contents -> bindContents(it)
                is SelectState.Error -> bindError(it)
                is SelectState.Loading -> bindLoading(it)
            }
        }
            .launchIn(lifecycleScope)
    }

    private fun bindContents(state: SelectState.Contents) {
        adapter.submitList(state.list.map { MenuUiModel(it) })
    }

    private fun bindLoading(state: SelectState.Loading) {

    }

    private fun bindError(state: SelectState.Error) {
        Timber.d("Error ${state}")
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<MenuUiModel>() {

        override fun areItemsTheSame(oldItem: MenuUiModel, newItem: MenuUiModel): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: MenuUiModel, newItem: MenuUiModel): Boolean {
            return oldItem == newItem
        }
    }

    inner class Adapter : ListAdapter<MenuUiModel, ViewHolder>(DiffUtilCallback()) {

        override fun submitList(list: List<MenuUiModel>?) {
            super.submitList(list?.toMutableList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent, this@SelectMenuFragment)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                holder.onBindView(getItem(position))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    inner class ViewHolder(parent: ViewGroup, fragment: Fragment) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_child_select_menu, parent, false)
    ) {
        private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }
        private val ivThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.ivThumb) }
        private var model: MenuUiModel? = null
        private val requestManager: RequestManager by lazy { Glide.with(fragment) }

        init {
            itemView.setOnClickListener {
                if (model == null) return@setOnClickListener
                val card = model?.data ?: return@setOnClickListener
                when (card) {
                    is Card.FragmentCard -> {
                        card.getTargetFragment()?.let {
                            fragment.parentFragmentManager.moveToFragment(it)
                        }
                    }

                    is Card.ActivityCard -> {
                        card.getTargetActivity()?.let {
                            itemView.context.startActivity(Intent(itemView.context, it.java))
                        }
                    }
                }
            }
        }

        fun onBindView(model: MenuUiModel) {
            this.model = model
            tvTitle.text = model.title
            when (val thumbContents = model.data.getThumbContents()) {
                is Card.ContentsType.WebP -> {
                    requestManager.load(thumbContents.bytes)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(ivThumb)
                }

                is Card.ContentsType.Images -> {
                    requestManager.load(thumbContents.bytes)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(ivThumb)
                }
            }
        }
    }
}
