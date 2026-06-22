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
import com.hmju.visual.R
import com.hmju.visual.databinding.ADialogBinding

/**
 * Description : DialogFragment 우선순위 노출 테스트 Activity.
 * FrameLayout 하나에 DialogARootFragment / DialogBRootFragment 를 add 해두고
 * show()/hide() + setMaxLifecycle 로 전환한다.
 * hide 된 쪽은 maxLifecycle 이 STARTED 로 내려가므로, DialogARootFragment 의
 * repeatOnLifecycle(RESUMED) 블록(시뮬레이션)도 A 가 hide 되면 함께 멈춘다.
 *
 * Created by juhongmin on 2026. 6. 21.
 */
class DialogActivity : AppCompatActivity() {

    private lateinit var binding: ADialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ADialogBinding>(this, R.layout.a_dialog).apply {
            lifecycleOwner = this@DialogActivity
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            WindowInsetsCompat.CONSUMED
        }
        if (savedInstanceState == null) {
            val fragmentA = DialogARootFragment()
            val fragmentB = DialogBRootFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, fragmentA, TAG_A)
                .add(R.id.fragmentContainer, fragmentB, TAG_B)
                .hide(fragmentB)
                .setMaxLifecycle(fragmentB, Lifecycle.State.STARTED)
                .commit()
        }
    }

    /** A/B 두 Root Fragment 를 show/hide 전환한다. 보이는 쪽만 RESUMED, 숨는 쪽은 STARTED. */
    fun switchPage(showB: Boolean) {
        val fragmentA = supportFragmentManager.findFragmentByTag(TAG_A) ?: return
        val fragmentB = supportFragmentManager.findFragmentByTag(TAG_B) ?: return
        supportFragmentManager.beginTransaction().apply {
            if (showB) {
                show(fragmentB).setMaxLifecycle(fragmentB, Lifecycle.State.RESUMED)
                hide(fragmentA).setMaxLifecycle(fragmentA, Lifecycle.State.CREATED)
            } else {
                show(fragmentA).setMaxLifecycle(fragmentA, Lifecycle.State.RESUMED)
                hide(fragmentB).setMaxLifecycle(fragmentB, Lifecycle.State.CREATED)
            }
        }.commit()
    }

    companion object {
        private const val TAG_A = "dialog_root_a"
        private const val TAG_B = "dialog_root_b"
    }
}
