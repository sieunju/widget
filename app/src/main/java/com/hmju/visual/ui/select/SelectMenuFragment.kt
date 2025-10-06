package com.hmju.visual.ui.select

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

/**
 * Description : Visual Ui Selection Menu Fragment
 *
 * Created by juhongmin on 2022/12/30
 */
internal class SelectMenuFragment : Fragment(R.layout.f_select_menu) {

    private lateinit var useCase: SelectionUseCase

    sealed interface UiModel {
        data class ContentsUiModel(
            val title: String,
            val data: Card
        ) : UiModel {
            constructor(data: Card) : this(
                title = when (data) {
                    is Card.FragmentCard -> data.title
                    is Card.ActivityCard -> data.title
                },
                data = data
            )
        }

        data class ShimmerUiModel(
            val uid: Long = System.currentTimeMillis()
        ) : UiModel
    }

    private lateinit var rvContents: RecyclerView

    private val adapter: Adapter by lazy { Adapter() }

    private val state: LiveData<SelectState> by lazy {
        useCase().asLiveData(lifecycleScope.coroutineContext)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        useCase = SelectionUseCase(GithubRepository(context.applicationContext))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContents = view.findViewById(R.id.rvContents)
        rvContents.layoutManager = GridLayoutManager(view.context, 2)
        rvContents.adapter = adapter

        state.observe(viewLifecycleOwner) {
            when(it) {
                is SelectState.Loading -> bindLoading()
                is SelectState.Contents -> bindContents(it)
                is SelectState.Error -> bindError(it)
            }
        }
    }

    private fun bindContents(state: SelectState.Contents) {
        adapter.submitList(state.list.map { UiModel.ContentsUiModel(it) })
    }

    private fun bindLoading() {
        adapter.submitList((0..5).map { UiModel.ShimmerUiModel() })
    }

    private fun bindError(state: SelectState.Error) {
        Timber.d("Error ${state}")
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<UiModel>() {

        override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
            return if (oldItem is UiModel.ContentsUiModel && newItem is UiModel.ContentsUiModel) {
                oldItem.title == newItem.title
            } else if (oldItem is UiModel.ShimmerUiModel && newItem is UiModel.ShimmerUiModel) {
                oldItem.uid == newItem.uid
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
            return if (oldItem is UiModel.ContentsUiModel && newItem is UiModel.ContentsUiModel) {
                oldItem == newItem
            } else if (oldItem is UiModel.ShimmerUiModel && newItem is UiModel.ShimmerUiModel) {
                oldItem == newItem
            } else {
                false
            }
        }
    }

    inner class Adapter : ListAdapter<UiModel, RecyclerView.ViewHolder>(DiffUtilCallback()) {

        override fun submitList(list: List<UiModel>?) {
            super.submitList(list?.toMutableList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                R.layout.vh_child_select_menu -> ContentsViewHolder(parent, this@SelectMenuFragment)
                R.layout.vh_child_select_menu_shimmer -> ShimmerViewHolder(parent)
                else -> throw IllegalArgumentException("Invalid View Type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            try {
                if (holder is ContentsViewHolder) {
                    holder.onBindView(getItem(position))
                } else if (holder is ShimmerViewHolder) {

                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
            return when (item) {
                is UiModel.ContentsUiModel -> R.layout.vh_child_select_menu
                else -> R.layout.vh_child_select_menu_shimmer
            }
        }
    }

    inner class ShimmerViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_child_select_menu_shimmer, parent, false)
    )

    inner class ContentsViewHolder(
        parent: ViewGroup,
        fragment: Fragment
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_child_select_menu, parent, false)
    ) {
        private val tvTitle: AppCompatTextView by lazy { itemView.findViewById(R.id.tvTitle) }
        private val ivThumb: AppCompatImageView by lazy { itemView.findViewById(R.id.ivThumb) }
        private var model: UiModel.ContentsUiModel? = null
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

        fun onBindView(model: UiModel) {
            if (model !is UiModel.ContentsUiModel) return
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
