package com.hmju.visual.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hmju.visual.databinding.FDialogRootBBinding

/**
 * Description : DialogPriorityManager 가 없는 비교용 Root Fragment.
 *
 * Created by juhongmin on 2026. 6. 22.
 */
internal class DialogBRootFragment : Fragment() {

    private var binding: FDialogRootBBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FDialogRootBBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
