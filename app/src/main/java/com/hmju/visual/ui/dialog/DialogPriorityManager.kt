package com.hmju.visual.ui.dialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 우선순위 기반 DialogFragment 표시 매니저.
 *
 * - priority 숫자가 낮을수록 중요도 높음 (1 = 최상위, 항상 가장 위에 노출)
 * - 모든 enqueue 호출을 [DEBOUNCE_MS] 동안 모은 뒤 한 번에 정렬해서 show
 * - 이미 show 된 상태에서 더 낮은 우선순위 다이얼로그가 들어오면 전체를 재정렬
 *   (기존 다이얼로그 dismiss → 우선순위 순으로 re-show)
 */
internal class DialogPriorityManager(
    private val fm: FragmentManager,
    private val scope: CoroutineScope
) {

    private data class Entry(val priority: Int, val tag: String, val create: () -> DialogFragment)

    private val registry = mutableListOf<Entry>()
    private var debounceJob: Job? = null
    private var isFlushing = false

    /** 마지막 flush() 결과 show 순서 (마지막 원소 = 시각적 최상단) */
    private var shownOrder: List<String> = emptyList()

    init {
        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (isFlushing) return
                registry.removeAll { it.tag == f.tag }
                shownOrder = shownOrder.filterNot { it == f.tag }
            }
        }, false)
    }

    fun enqueue(priority: Int, tag: String, create: () -> DialogFragment) {
        val existing = registry.find { it.tag == tag }
        if (existing != null && existing.priority == priority && isTopmost(tag)) {
            // 이미 같은 우선순위로 최상단에 노출 중 → re-show 불필요
            return
        }
        registry.removeAll { it.tag == tag }
        registry.add(Entry(priority, tag, create))
        debounceJob?.cancel()
        debounceJob = scope.launch(Dispatchers.Main) {
            delay(DEBOUNCE_MS)
            flush()
        }
    }

    private fun isTopmost(tag: String): Boolean =
        shownOrder.lastOrNull() == tag && fm.findFragmentByTag(tag) != null

    private fun flush() {
        isFlushing = true
        val snapshot = registry.toList()

        // 현재 보여지는 관리 대상 다이얼로그 모두 제거
        val tx = fm.beginTransaction()
        snapshot.forEach { entry -> fm.findFragmentByTag(entry.tag)?.let { tx.remove(it) } }
        tx.commitAllowingStateLoss()

        // priority 내림차순(D=4 먼저, A=1 마지막)으로 show → 마지막 show = 시각적 최상단
        // show() 내부의 commit() 대신 commitAllowingStateLoss() 사용 — onSaveInstanceState 이후에도 안전
        val order = snapshot.sortedByDescending { it.priority }
        order.forEach { entry ->
            fm.beginTransaction()
                .add(entry.create(), entry.tag)
                .commitAllowingStateLoss()
        }
        shownOrder = order.map { it.tag }

        isFlushing = false
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
