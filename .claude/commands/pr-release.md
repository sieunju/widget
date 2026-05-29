develop 브랜치와 master 브랜치를 비교하여 Release PR을 작성해줘.

먼저 아래 명령어로 변경사항을 확인해:
1. `git log origin/master..origin/develop --oneline --no-merges` (develop에만 있는 커밋)
2. `git diff origin/master...origin/develop --stat` (변경된 파일 목록)
3. 필요시 주요 변경 파일 내용 확인

아래 템플릿 형식을 **정확히** 따라서 작성해:

```markdown
## 🚀 Release

### 📋 포함된 변경 사항
<!-- develop → master 에 포함되는 PR/커밋 목록 -->

### 🔎 주요 변경 사항
- [ ] UI/UX 변경
- [ ] API 추가/변경
- [ ] DB Scheme 변경
- [ ] Permission 관련 변경
- [ ] Bug Fix
- [ ] 기타

### 🛠 배포 내용
<!-- 이번 릴리즈에서 배포되는 기능/수정 사항을 bullet point로 정리 -->

### ✅ 배포 전 체크리스트
- [ ] 테스트 완료
- [ ] DB 마이그레이션 확인
- [ ] 환경변수/설정 변경 사항 없음
- [ ] 의존성 변경 사항 없음

### 🤔 특이사항
<!-- 배포 시 주의할 점이나 공유할 내용. 없으면 "없음" -->

### 🐯 Etc.
<!-- 기타 공유할 내용. 없으면 비워둬 -->
```

작성 규칙:
1. 주요 변경 사항은 해당하는 항목에 `[x]` 체크
2. 포함된 변경 사항은 커밋/PR 단위로 목록화
3. 배포 내용은 기술적 내용보다 기능/사용자 관점으로 작성
4. 한국어로 작성
5. 마크다운 코드블록 없이 바로 출력
6. 작성 완료 후 `.claude/pr-text.md` 파일에 저장
