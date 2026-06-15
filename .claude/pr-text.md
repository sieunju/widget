## 🤩 Feature

### 🔎 주요 변경 사항
- [x] UI/UX 변경
- [ ] API 추가/변경
- [ ] DB Scheme 변경
- [ ] Permission 관련 변경
- [ ] 기타

### 🛠 구현한 기능

**CustomSnackBar 신규 구현 (`snackbar` 모듈)**
- `WidgetSnackBar` 제거 → `CustomSnackBar` 로 완전 대체
- Activity / Fragment 스코프 지원 (`with(activity)` / `with(fragment)`)
  - Fragment 스코프: `ON_DESTROY` 시 자동 dismiss
- 2가지 Style 지원
  - `FLOAT`: 둥글둥글한 플로팅 카드 — 흰 배경, Corner 16dp, Padding 16dp, 그림자
  - `BANNER`: Full-width 배너 — TOP 은 StatusBar 오버랩, BOTTOM 은 NavBar 오버랩
- Top / Bottom 위치 지정
- 연속 이벤트 처리: dismiss / show 독립 동시 실행 (화면에 2개 동시 존재 가능)
- `OnDismissListener` / `OnShowListener` 독립 발화
- `Handler` → Coroutine (`CoroutineScope + delay + Job`) 전환
- `snackbar` 모듈 신규 추가 (`settings.gradle`, `build.gradle`)

**테스트 예제 (`app` 모듈)**
- `SnackBarActivity` / `SnackBarFragment` 추가
- FLOAT Bottom / Top, BANNER Bottom / Top, 연속 이벤트 테스트 버튼
- `selection.json` 메뉴 항목 추가

### 🕹 테스트 결과
- FLOAT Bottom / Top 슬라이드 애니메이션 확인
- BANNER Top StatusBar 오버랩 확인
- BANNER Bottom NavBar 오버랩 확인
- 연속 이벤트: 이전 dismiss + 신규 show 동시 슬라이드 확인
- Fragment 이탈 시 자동 dismiss 확인

### 🤔 고민되는 사항
- 연속 이벤트 발생 시 dismiss 와 show 가 동시에 실행되어 화면에 2개의 SnackBar 가 잠깐 공존함

### 🐮 Etc.
