package hmju.widget.tablayout

object Extensions {
    /**
     * 외 / 내부 여러 변수들의 NullCheck 를 하고자 무분별한 ?.let or ?.run 남용을 막기위해
     * 만든 함수 3개의 변수를 체크하는 함수
     * @return let 확장함수와 동일하게 고차함수 중간에 리턴 형태를 변경할수 있다.
     */
    inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
        return if (a != null && b != null) {
            function(a, b)
        } else {
            null
        }
    }
}
