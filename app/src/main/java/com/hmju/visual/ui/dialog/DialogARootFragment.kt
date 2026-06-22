package com.hmju.visual.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hmju.visual.databinding.FDialogRootABinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Description : DialogPriorityManager 가 종속되는 Root Fragment.
 * 매니저는 childFragmentManager + viewLifecycleOwner.lifecycleScope 로 동작하므로
 * 이 Fragment 의 뷰가 destroy 되면 debounce job 등도 함께 정리된다.
 *
 * Created by juhongmin on 2026. 6. 22.
 */
internal class DialogARootFragment : Fragment() {

    private var binding: FDialogRootABinding? = null
    private var manager: DialogPriorityManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FDialogRootABinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = DialogPriorityManager(childFragmentManager, viewLifecycleOwner.lifecycleScope)
        initButton()
        Timber.d("DialogARoot onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        manager = null
        binding = null
        Timber.d("DialogARoot onDestroyView")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("DialogARoot onStop")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("DialogARoot onPause")
    }

    private fun initButton() {
        val binding = binding ?: return
        binding.btnSimulateAll.setOnClickListener { simulateAll() }
        binding.btnSimulateLateA.setOnClickListener { simulateLateA() }
        binding.btnShowA.setOnClickListener { manager?.enqueue(1, "dialog_a") { ADialogFragment() } }
        binding.btnShowB.setOnClickListener { manager?.enqueue(2, "dialog_b") { BDialogFragment() } }
        binding.btnShowC.setOnClickListener { manager?.enqueue(3, "dialog_c") { CDialogFragment() } }
        binding.btnShowD.setOnClickListener { manager?.enqueue(4, "dialog_d") { DDialogFragment() } }
    }

    /** 시나리오 1: A~D 가 거의 동시에 API 응답 → debounce 300ms 내에 수집 후 정렬 show */
    private fun simulateAll() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                manager?.enqueue(3, "dialog_c") { CDialogFragment() }
                delay(150)
                manager?.enqueue(1, "dialog_a") { ADialogFragment() }
                delay(530)
                manager?.enqueue(4, "dialog_d") { DDialogFragment() }
                delay(250)
                manager?.enqueue(2, "dialog_b") { BDialogFragment() }
            }

        }
        lifecycleScope.launch {
            switchToB()
            delay(5000)
            switchToA()
        }
    }

    /** 시나리오 2: D, C 먼저 응답 → A 가 600ms 뒤에 응답 → A 가 최상단에 위치해야 함 */
    private fun simulateLateA() {
        viewLifecycleOwner.lifecycleScope.launch {
            manager?.enqueue(4, "dialog_d") { DDialogFragment() }
            delay(100); manager?.enqueue(3, "dialog_c") { CDialogFragment() }
            delay(500); manager?.enqueue(1, "dialog_a") { ADialogFragment() }
        }
    }

    /** B 페이지로 전환한다. A 는 hide 되며 maxLifecycle 이 STARTED 로 내려가 이 시뮬레이션도 함께 멈춘다. */
    private fun switchToB() {
        (requireActivity() as DialogActivity).switchPage(showB = true)
    }

    private fun switchToA(){
        (requireActivity() as DialogActivity).switchPage(showB = false)
    }
}
