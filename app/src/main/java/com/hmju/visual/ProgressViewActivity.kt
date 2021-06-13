package com.hmju.visual

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import hmju.widget.progress.ProgressView
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class ProgressViewActivity : AppCompatActivity() {

    private val progressView : ProgressView by lazy { findViewById<ProgressView>(R.id.progressView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_view)

        findViewById<AppCompatTextView>(R.id.tvStart).setOnClickListener {
            Observable.fromCallable {
                for (index in 0 until 100) {
                    progressView.incrementProgressBy(1)
                }
            }.subscribeOn(Schedulers.computation()).subscribe()
        }
        findViewById<AppCompatTextView>(R.id.tvInit).setOnClickListener {
            progressView.currentProgress = 0
        }
    }
}