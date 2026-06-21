package com.hmju.visual.ui.dialog

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * 우선순위 기반 DialogFragment 표시 매니저.
 *
 * - priority 숫자가 낮을수록 중요도 높음 (1 = 최상위, 항상 가장 위에 노출)
 * - 모든 enqueue 호출을 [DEBOUNCE_MS] 동안 모은 뒤 한 번에 정렬해서 show
 * - 이미 show 된 상태에서 더 낮은 우선순위 다이얼로그가 들어오면 전체를 재정렬
 *   (기존 다이얼로그 dismiss → 우선순위 순으로 re-show)
 */
internal class DialogPriorityManager(private val fm: FragmentManager) {

    private data class Entry(val priority: Int, val tag: String, val create: () -> DialogFragment)

    private val registry = mutableListOf<Entry>()
    private val handler = Handler(Looper.getMainLooper())
    private val flushRunnable = Runnable { flush() }
    private var isFlushing = false

    init {
        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (isFlushing) return
                registry.removeAll { it.tag == f.tag }
            }
        }, false)
    }

    fun enqueue(priority: Int, tag: String, create: () -> DialogFragment) {
        registry.removeAll { it.tag == tag }
        registry.add(Entry(priority, tag, create))
        handler.removeCallbacks(flushRunnable)
        handler.postDelayed(flushRunnable, DEBOUNCE_MS)
    }

    private fun flush() {
        isFlushing = true
        val snapshot = registry.toList()

        // 현재 보여지는 관리 대상 다이얼로그 모두 제거
        val tx = fm.beginTransaction()
        snapshot.forEach { entry -> fm.findFragmentByTag(entry.tag)?.let { tx.remove(it) } }
        tx.commitAllowingStateLoss()
        fm.executePendingTransactions()

        // priority 내림차순(D=4 먼저, A=1 마지막)으로 show → 마지막 show = 시각적 최상단
        snapshot.sortedByDescending { it.priority }.forEach { entry ->
            entry.create().show(fm, entry.tag)
            fm.executePendingTransactions()
        }

        isFlushing = false
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
