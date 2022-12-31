package com.hmju.visual.ui.coordinator

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.hmju.visual.R

internal class TranslationBehaviorFragment : Fragment(R.layout.f_translation_behavior) {

    private lateinit var tvBeforeTitle: AppCompatTextView
    private lateinit var tvAfterTitle: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            tvBeforeTitle = findViewById(R.id.tvBeforeTitle)
            tvAfterTitle = findViewById(R.id.tvAfterTitle)
            tvBeforeTitle.text = "Hello"
            tvAfterTitle.text = "It's Me..."
        }
    }
}