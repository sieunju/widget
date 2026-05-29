package com.hmju.visual.ui.snackbar

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.hmju.visual.R
import hmju.widget.snackbar.CustomSnackBar

/**
 * Description : CustomSnackBar 테스트 Fragment
 *
 * Created by juhongmin
 */
internal class SnackBarFragment : Fragment(R.layout.f_snack_bar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<AppCompatButton>(R.id.btnShowBottom).setOnClickListener {
            CustomSnackBar.with(requireContext())
                .message("Bottom SnackBar 입니다.")
                .position(CustomSnackBar.Position.BOTTOM)
                .show()
        }
        view.findViewById<AppCompatButton>(R.id.btnShowTop).setOnClickListener {
            CustomSnackBar.with(requireContext())
                .message("Top SnackBar 입니다.")
                .position(CustomSnackBar.Position.TOP)
                .show()
        }
        view.findViewById<AppCompatButton>(R.id.btnShowConsecutive).setOnClickListener {
            // 연속 이벤트 테스트: 300ms 간격으로 3회 호출
            val messages = listOf("첫 번째 메시지", "두 번째 메시지 (중간 드롭)", "세 번째 메시지 (최종)")
            messages.forEachIndexed { index, msg ->
                view.postDelayed({
                    CustomSnackBar.with(requireContext())
                        .message(msg)
                        .position(CustomSnackBar.Position.BOTTOM)
                        .show()
                }, index * 300L)
            }
        }
        view.findViewById<AppCompatButton>(R.id.btnShowTopConsecutive).setOnClickListener {
            val messages = listOf("Top - 첫 번째", "Top - 두 번째", "Top - 세 번째")
            messages.forEachIndexed { index, msg ->
                view.postDelayed({
                    CustomSnackBar.with(requireContext())
                        .message(msg)
                        .position(CustomSnackBar.Position.TOP)
                        .show()
                }, index * 300L)
            }
        }
        view.findViewById<AppCompatButton>(R.id.btnShowLong).setOnClickListener {
            CustomSnackBar.with(requireContext())
                .message("duration 5초짜리 Bottom SnackBar 입니다. 5초 후 자동으로 사라집니다.")
                .position(CustomSnackBar.Position.BOTTOM)
                .duration(5000L)
                .show()
        }
    }
}
