package com.hmju.visual.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hmju.visual.R
import com.hmju.visual.databinding.ADialogBinding

/**
 * Description : 
 *
 * Created by juhongmin on 2026. 6. 21.
 */
class DialogActivity : AppCompatActivity() {

    private lateinit var binding : ADialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ADialogBinding>(this,R.layout.a_dialog).apply {
            lifecycleOwner = this@DialogActivity
        }
    }

}
