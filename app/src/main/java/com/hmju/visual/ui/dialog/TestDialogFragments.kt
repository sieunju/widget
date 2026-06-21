package com.hmju.visual.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatButton
import com.hmju.visual.R

internal class ADialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.f_a_dialog, null)
        view.findViewById<AppCompatButton>(R.id.btnClose).setOnClickListener { dismiss() }
        return Dialog(requireContext()).apply {
            setContentView(view)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}

internal class BDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.f_b_dialog, null)
        view.findViewById<AppCompatButton>(R.id.btnClose).setOnClickListener { dismiss() }
        return Dialog(requireContext()).apply {
            setContentView(view)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }
    }
}

internal class CDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("[C] Priority 3 — 상단 노출")
            .setMessage("C 다이얼로그입니다.")
            .setPositiveButton("닫기") { _, _ -> dismiss() }
            .create()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.TOP)
        }
    }
}

internal class DDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("[D] Priority 4 — 최하위 다이얼로그")
            .setMessage("D는 항상 다른 다이얼로그 아래에 노출됩니다.")
            .setPositiveButton("닫기") { _, _ -> dismiss() }
            .create()
}
