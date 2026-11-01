## 🤩 Feature

### 🔎 주요 변경 사항
- [x] UI/UX 변경
- [ ] API 추가/변경
- [ ] DB Scheme 변경
- [ ] Permission 관련 변경
- [x] 기타

### 🛠 구현한 기능
- `BlurMirrorView` 추가
  - 뒤쪽 `sourceView`(ex. RecyclerView)의 그리기 결과를 화면 좌표에 맞춰 그대로 다시 그리는 View
  - 헤더 영역에 겹쳐 두고 블러를 적용하면 뒤로 흐르는 콘텐츠가 실시간으로 흐려 보임
- `BlurStrategy` 인터페이스 및 API Level별 구현체 추가
  - `RenderEffectBlurStrategy` (API 31+) : `RenderEffect` 가우시안 블러 + 채도 보정 + 오버레이 컬러 블렌딩
    - 균일 블러(radiusX = radiusY)를 `blurPasses` 횟수만큼 체이닝해서 번짐 강도 조절
    - `TileMode.MIRROR` 적용으로 가장자리 아티팩트 완화
    - `OnPreDrawListener`를 등록해서 스크롤, 이미지 로딩, 애니메이션과 관계없이 매 프레임 블러 갱신
    - `setRadius()` / `setOverlayColor()` / `setStyle()`로 런타임 변경 지원
  - `DimOnlyStrategy` (API 31 미만) : 실시간 블러 대신 반투명 배경색만 적용해서 frosted glass 느낌만 냄
- `BlurStyle` 프리셋 추가 (iOS `UIBlurEffectStyle` 대응)
  - `LIGHT` / `EXTRA_LIGHT` / `DARK` / `PROMINENT` : radius, saturation, overlayColor 조합
- `Extensions.create(style)` : 디바이스 API Level에 맞는 `BlurStrategy` 반환
- 샘플 앱
  - `BlurHeaderActivity` 테스트 화면 추가 (GridLayoutManager RecyclerView + 블러 헤더)
  - `selection.json` / `Card` 라우터 / `AndroidManifest`에 `BlurHeaderActivity` 등록
  - `GithubRepository`에 `HttpLoggingInterceptor`(BODY 레벨) 추가
  - kotlinx.serialization `InternalSerializationApi` opt-in 및 `withTimeoutOrNull` Duration 오버로드로 변경

### 🕹 테스트 결과
- 샘플 앱의 `BlurHeader` 카드에서 `BlurHeaderActivity` 실행
- API 31+ 기기에서 RecyclerView를 스크롤할 때 헤더 아래 콘텐츠가 실시간으로 블러 처리되는지 확인
- 디버그 캡처 프리뷰(`captureFrame`)로 미러링 좌표와 캡처 범위가 맞는지 확인한 후 디버그 코드 제거

### 🤔 고민되는 사항
- API 31 미만은 비트맵 캡처 방식의 실시간 블러가 성능, 발열 대비 효과가 낮다고 판단해서 반투명 배경(`DimOnlyStrategy`)으로만 대체함
- `OnPreDrawListener`로 매 프레임 `invalidate`하기 때문에 정적인 화면에서도 다시 그리는 비용이 발생함
- `BlurMirrorView`의 `sourceView`가 자기 자신의 하위 View가 되면 무한 재귀가 발생하므로 사용 시 주의가 필요함

### 🐮 Etc.
- `HttpLoggingInterceptor`가 빌드 타입 구분 없이 BODY 레벨로 추가되어 있음 (디버그 전용으로 제한할지 확인 필요)
- `selection.json`의 BlurHeader 썸네일은 임시로 `example_dynamic_coordinator.webp`를 사용함
