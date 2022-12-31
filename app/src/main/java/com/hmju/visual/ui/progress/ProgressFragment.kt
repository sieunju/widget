package com.hmju.visual.ui.progress

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.hmju.visual.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ProgressFragment : Fragment(R.layout.fragment_progress) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val progressView: hmju.widget.progress.ProgressView = findViewById(R.id.progressView)
            findViewById<AppCompatTextView>(R.id.tvStart).setOnClickListener {
                GlobalScope.launch(Dispatchers.Default) {
                    repeat(100) {
                        progressView.incrementProgressBy(1)
                        delay(10)
                    }
                }
            }
            findViewById<AppCompatTextView>(R.id.tvInit).setOnClickListener {
                progressView.currentProgress = 0
            }
        }

    }
}