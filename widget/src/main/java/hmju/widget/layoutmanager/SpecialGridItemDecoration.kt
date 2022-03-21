package hmju.widget.layoutmanager

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Description : 특정 타입만 Grid Decoration 처리하는 클래스
 * GridLayout SpanCount 2로 한정내에서 처리
 * @param horizontalDivider 좌우 사이 간격
 * @param verticalDivider 상하 사이 간격 (TOP 을 기준으로 한다)
 * @param side 좌 / 우 여백
 * 내 능력상 Grid SpanCnt 2 만 가능함...
 * Created by juhongmin on 2022/03/21
 */
class SpecialGridItemDecoration(
    private val horizontalDivider: Int = 0,
    private val verticalDivider: Int = 0,
    private val side: Int = 0,
    private val fixSpanCount: Int = 2,
    gridTypeList: List<Int> = listOf()
) : RecyclerView.ItemDecoration() {

    companion object {
        private const val TAG = "GridItem"
        private const val DEBUG = true
        fun LogD(msg: String) {
            if (DEBUG) {
                Log.d(TAG, msg)
            }
        }

        /**
         * 특정 레이아웃만 Grid Span 1 로 리턴하고 나머지는 1로 리턴 처리하는 함수
         * @param gridTypeList Grid Span 1 로 리턴할 레이아웃 아이디
         * @param spanCnt Span Cnt 현재 무조건 2여야 함
         */
        fun RecyclerView.spanSpecialGridType(gridTypeList: List<Int>, spanCnt: Int) {
            val gridTypeMap = HashMap<Int, Boolean>()
            gridTypeMap.putAll(gridTypeList.map { it to true })
            if (layoutManager is GridLayoutManager) {
                (layoutManager as GridLayoutManager).runCatching {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(pos: Int): Int {
                            return adapter?.getItemViewType(pos)?.let { viewType ->
                                if (gridTypeMap[viewType] == true) {
                                    1
                                } else {
                                    spanCnt
                                }
                            } ?: run {
                                spanCnt
                            }
                        }
                    }.apply {
                        isSpanIndexCacheEnabled = true
                        isSpanGroupIndexCacheEnabled = true
                    }
                }
            }
        }
    }

    private val gridTypeMap: HashMap<Int, Boolean> by lazy { HashMap() }

    init {
        gridTypeMap.putAll(gridTypeList.map { it to true })
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        runCatching {
            outRect.setEmpty()

            val pos = parent.getChildAdapterPosition(view)
            val viewType = parent.adapter?.getItemViewType(pos) ?: 0

            // Grid 를 표시해야하는 레이아웃
            if (gridTypeMap[viewType] == true) {
                val column = getColumnIndex(pos, fixSpanCount, parent)
                // val row = getRowIndex(pos, fixSpanCount, parent)
                // val lastRow = getRowIndex(0.coerceAtLeast(state.itemCount), parent).minus(1)
                // LogD("Pos $pos Row $row Column $column  LastRow $lastRow")
                // [s] 어떤 SpanCnt 가 와도 균등하게 좌우 여백을 처리해주는 로직 하지만, 맨 왼쪽, 오른쪽 여백을 처리하는 경우 해당 로직 X
                // outRect.left = column * horizontalDivider / fixSpanCount
                // outRect.right = horizontalDivider - column.plus(1) * horizontalDivider / fixSpanCount
                // [s] 어떤 SpanCnt 가 와도 균등하게 좌우 여백을 처리해주는 로직 하지만, 맨 왼쪽, 오른쪽 여백을 처리하는 경우 해당 로직 X

                // SpanCnt == 2 고정
                when (column) {
                    0 -> {
                        // 맨 왼쪽
                        outRect.left = side
                        outRect.right = horizontalDivider - (horizontalDivider / fixSpanCount)
                    }
                    else -> {
                        outRect.left = (column * horizontalDivider) / fixSpanCount
                        outRect.right = side
                    }
                }
                val beforePos = if (column == 0) pos.minus(1) else pos.minus(2)
                val beforeRowViewType = parent.adapter?.getItemViewType(beforePos) ?: 0

                // GridType 이 아닌경우 첫번쨰 Row
                if(gridTypeMap[beforeRowViewType] == null) {
                    LogD("첫번째 Grid 입니다. $pos")
                } else {
                    LogD("Grid Type 속에 있습니다. $pos")
                    outRect.top = verticalDivider
                }
            }
        }.onFailure {
            LogD("Error $it")
        }
    }

    /**
     * 해당 포지션이 Row 기준 몇번쨰인지 리턴하는 함수
     */
    private fun getRowIndex(pos: Int, spanCnt: Int, parent: RecyclerView): Int {
        return try {
            (parent.layoutManager as GridLayoutManager).run {
                return spanSizeLookup.getSpanGroupIndex(pos, spanCnt)
            }
        } catch (ex: Exception) {
            0
        }
    }

    private fun getColumnIndex(pos: Int, spanCnt: Int, parent: RecyclerView): Int {
        return try {
            (parent.layoutManager as GridLayoutManager).run {
                val spanIdx = spanSizeLookup.getSpanIndex(pos, spanCnt)
                return spanIdx % spanCnt
            }
        } catch (ex: Exception) {
            0
        }
    }
}