package com.hmju.visual

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import hmju.widget.progress.ProgressView
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


class ProgressFragment(): Fragment(R.layout.fragment_progress) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val progressView : ProgressView = findViewById(R.id.progressView)
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
}