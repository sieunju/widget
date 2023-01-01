package com.hmju.visual.ui.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Description :
 *
 * Created by juhongmin on 2023/01/01
 */
class VerticalItemDecoration(
    private var side: Int = 0,
    private var divider: Int = 0,
    private var topSpace: Int = 0,
    private var bottomSpace: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val pos = parent.getChildAdapterPosition(view)
        val cnt = state.itemCount

        // 맨 왼쪽 & 맨 오른쪽 간격 처리
        if (side != 0) {
            outRect.left = side
            outRect.right = side
        }

        // 맨위에 따로 간격을 처리해야 한다면 topSpace 값으로 처리
        if (pos == 0 && topSpace != 0) {
            outRect.top = topSpace
        } else if (pos != 0 && divider != 0) {
            // 이외에는 TOP 기준으로 사이 간격 처리
            outRect.top = divider
        }

        // 맨 마지막 아이템에 간격 처리 해야 한다면 bottomSpace 값으로 처리
        if (pos == cnt - 1 && bottomSpace != 0) {
            outRect.bottom = bottomSpace
        }
    }
}