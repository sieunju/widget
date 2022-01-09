package hmju.widget.tablayout

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import hmju.widget.extensions.dp

/**
 * Description : Pager TabItem
 * @param title 탭 이름 (데이터 모델 생성시 해당 파라미터만 설정하고 나머지는 Attr 로 처리합니다.)
 * @param pos 탭 위치 값
 * @param txtSize 탭 텍스트 사이즈
 * @param txtColor 택 텍스트 활성화 색상
 * @param disableTxtColor 탭 텍스트 비활성화 색상
 * @param view Binding View
 * Created by juhongmin on 2021/12/31
 */
data class PagerTabItem(
    val title: String,
    var pos: Int = 0,
    @Dimension var txtSize: Int = 16.dp,
    @ColorInt var txtColor: Int = Color.BLACK,
    @ColorInt var disableTxtColor: Int = Color.BLACK,
    var view: View? = null
) {
    // 해당 탭 선택 유부 LiveData
    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }
}