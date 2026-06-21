package com.hmju.visual.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hmju.visual.R
import com.hmju.visual.databinding.ADialogBinding

/**
 * Description : DialogFragment 우선순위 노출 테스트 Activity
 *
 * Created by juhongmin on 2026. 6. 21.
 */
class DialogActivity : AppCompatActivity() {

    private lateinit var binding: ADialogBinding
    private lateinit var manager: DialogPriorityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ADialogBinding>(this, R.layout.a_dialog).apply {
            lifecycleOwner = this@DialogActivity
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, object : OnApplyWindowInsetsListener {
            override fun onApplyWindowInsets(
                v: View,
                insets: WindowInsetsCompat
            ): WindowInsetsCompat {
                val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                v.updatePadding(top = statusBar.top)
                return WindowInsetsCompat.CONSUMED
            }
        })
        manager = DialogPriorityManager(supportFragmentManager, lifecycleScope)
        initButton()
    }

    private fun initButton() {
        binding.btnSimulateAll.setOnClickListener { simulateAll() }
        binding.btnSimulateLateA.setOnClickListener { simulateLateA() }
        binding.btnShowA.setOnClickListener { manager.enqueue(1, "dialog_a") { ADialogFragment() } }
        binding.btnShowB.setOnClickListener { manager.enqueue(2, "dialog_b") { BDialogFragment() } }
        binding.btnShowC.setOnClickListener { manager.enqueue(3, "dialog_c") { CDialogFragment() } }
        binding.btnShowD.setOnClickListener { manager.enqueue(4, "dialog_d") { DDialogFragment() } }
    }

    /** 시나리오 1: A~D 가 거의 동시에 API 응답 → debounce 300ms 내에 수집 후 정렬 show */
    private fun simulateAll() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(2000)
                manager.enqueue(3, "dialog_c") { CDialogFragment() }
                delay(150)
                manager.enqueue(1, "dialog_a") { ADialogFragment() }
                delay(530)
                manager.enqueue(4, "dialog_d") { DDialogFragment() }
                delay(250)
                manager.enqueue(2, "dialog_b") { BDialogFragment() }
            }
        }
    }

    /** 시나리오 2: D, C 먼저 응답 → A 가 600ms 뒤에 응답 → A 가 최상단에 위치해야 함 */
    private fun simulateLateA() {
        lifecycleScope.launch {
            manager.enqueue(4, "dialog_d") { DDialogFragment() }
            delay(100); manager.enqueue(3, "dialog_c") { CDialogFragment() }
            delay(500); manager.enqueue(1, "dialog_a") { ADialogFragment() }
        }
    }
}
