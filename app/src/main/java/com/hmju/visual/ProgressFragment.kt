package com.hmju.visual

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import hmju.widget.progress.ProgressView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProgressFragment : Fragment(R.layout.fragment_progress) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val progressView: ProgressView = findViewById(R.id.progressView)
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