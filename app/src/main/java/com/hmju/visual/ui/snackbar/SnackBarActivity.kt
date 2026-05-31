package com.hmju.visual.ui.snackbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hmju.visual.R

/**
 * Description : CustomSnackBar 테스트 Activity
 *
 * Created by juhongmin
 */
internal class SnackBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_snack_bar)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SnackBarFragment())
                .commit()
        }
    }
}
