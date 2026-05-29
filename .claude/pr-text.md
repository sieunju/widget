## 🤩 Feature

### 🔎 주요 변경 사항
- [x] UI/UX 변경
- [ ] API 추가/변경
- [ ] DB Scheme 변경
- [ ] Permission 관련 변경
- [ ] 기타

### 🛠 구현한 기능
- `view` 모듈에 `CustomSwitchView` 추가 — Apple(iOS) 스타일의 토글 스위치 커스텀 뷰
- Canvas 기반 직접 드로잉 (외부 라이브러리 없음)
- `FastOutSlowInInterpolator` 기반 thumb 슬라이드 애니메이션 + track 색상 전환 애니메이션
- `TouchDelegate`를 내부에서 자동 설정하여 터치 영역 확장 지원 (`switchTouchPadding`)
- XML attrs 지원: `switchChecked`, `switchTrackColorOn`, `switchTrackColorOff`, `switchThumbColor`, `switchThumbSize`, `switchThumbMargin`, `switchAnimDuration`, `switchTouchPadding`
- Kotlin API: `isChecked` 프로퍼티, `setOnCheckedChangeListener { isChecked -> }`
- `app` 모듈 `CustomViewFragment` 예제 화면에 스위치 + 상태 텍스트(ON/OFF) 추가

### 🕹 테스트 결과
- `CustomViewFragment` 하단에서 스위치 토글 시 ON/OFF 텍스트 변경 확인
- `switchThumbSize` / `switchThumbMargin` 커스텀 값 적용 시 뷰 크기 자동 계산 확인
- `switchTouchPadding` 설정 시 터치 영역 확장 확인 (뷰 시각적 크기는 유지)

### 🤔 고민되는 사항
- `switchTouchPadding` 은 `TouchDelegate`를 부모에 설정하는 방식이라 부모가 이미 `TouchDelegate`를 사용하는 경우 덮어쓰기 발생 가능

### 🐮 Etc.
