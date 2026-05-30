package com.hmju.visual.ui.snackbar

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.hmju.visual.R
import hmju.widget.snackbar.CustomSnackBar

internal class SnackBarFragment : Fragment(R.layout.f_snack_bar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── FLOAT ──────────────────────────────────────────────────────────
        view.findViewById<AppCompatButton>(R.id.btnFloatBottom).setOnClickListener {
            CustomSnackBar.with(this)
                .message("FLOAT · Bottom SnackBar")
                .position(CustomSnackBar.Position.BOTTOM)
                .style(CustomSnackBar.Style.FLOAT)
                .show()
        }
        view.findViewById<AppCompatButton>(R.id.btnFloatTop).setOnClickListener {
            CustomSnackBar.with(this)
                .message("FLOAT · Top SnackBar")
                .position(CustomSnackBar.Position.TOP)
                .style(CustomSnackBar.Style.FLOAT)
                .show()
        }

        // ── BANNER ─────────────────────────────────────────────────────────
        view.findViewById<AppCompatButton>(R.id.btnBannerBottom).setOnClickListener {
            CustomSnackBar.with(this)
                .message("BANNER · Bottom — NavBar 오버랩")
                .position(CustomSnackBar.Position.BOTTOM)
                .style(CustomSnackBar.Style.BANNER)
                .show()
        }
        view.findViewById<AppCompatButton>(R.id.btnBannerTop).setOnClickListener {
            CustomSnackBar.with(this)
                .message("BANNER · Top — StatusBar 오버랩안녕하세요 반갑습니다. 2줄입니다.")
                .position(CustomSnackBar.Position.TOP)
                .style(CustomSnackBar.Style.BANNER)
                .show()
        }

        // ── 연속 이벤트 ────────────────────────────────────────────────────
        view.findViewById<AppCompatButton>(R.id.btnConsecutive).setOnClickListener {
            listOf(
                "연속 이벤트 1번째",
                "연속 이벤트 2번째 (동시 슬라이드)",
                "연속 이벤트 3번째 (최종)"
            ).forEachIndexed { i, msg ->
                view.postDelayed({
                    CustomSnackBar.with(this)
                        .message(msg)
                        .position(CustomSnackBar.Position.BOTTOM)
                        .style(CustomSnackBar.Style.FLOAT)
                        .show()
                }, i * 400L)
            }
        }
    }
}
