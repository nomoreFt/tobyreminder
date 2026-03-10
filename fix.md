# TobyReminder Fix Checklist

> 코드 리뷰에서 발견된 이슈 목록. 우선순위 순 정렬.

---

## 🔴 즉시 수정 (버그 / 데이터 손상 위험)

### 낙관적 업데이트 롤백
- [x] `AppContext.tsx` — `toggleComplete()`: API 실패 시 제거한 reminder를 상태에 복원
- [x] `Sidebar.tsx` — `handleDragEnd()`: reorderLists API 실패 시 lists 순서 원복
- [x] `ReminderList.tsx` — `handleDragEnd()`: reorderReminders API 실패 시 reminders 순서 원복

### 데이터 무결성
- [x] `DefaultReminderService.kt` — `reorderReminders()`: 전달된 reminder IDs가 실제 해당 listId 소속인지 검증 추가
- [x] `DefaultReminderService.kt` — `reorderReminders()`: 전달된 IDs 수가 실제 목록 크기와 일치하는지 검증

### 누락 구현
- [x] `SmartLists.tsx` — 오늘/예정/플래그됨 배지 숫자를 실제 API 데이터로 연동 (현재 하드코딩 0)

---

## 🟠 단기 수정 (에러 처리 / 안정성)

### 백엔드 예외 처리
- [x] `GlobalExceptionHandler.kt` — `DataIntegrityViolationException` 핸들러 추가 (→ 400)
- [x] `GlobalExceptionHandler.kt` — `HttpMessageNotReadableException` 핸들러 추가 (→ 400)
- [x] `GlobalExceptionHandler.kt` — 제네릭 `Exception` fallback 핸들러 추가 (→ 500)
- [x] `GlobalExceptionHandler.kt` — 예외 발생 시 서버 로그 기록 추가

### 프론트엔드 에러 처리
- [ ] `AppContext.tsx` — 모든 async useCallback에 try/catch 추가
- [ ] `AppContext.tsx` — API 실패 시 사용자에게 토스트/알림 피드백 표시
- [x] `api.ts` — fetch 요청에 timeout 설정 (AbortController)
- [x] `page.tsx` — React Error Boundary 추가

### 입력 검증
- [x] `ReminderRequest.kt` — `@NotBlank(title)`, `@Size(max=...)` Bean Validation 추가
- [x] `ReminderListRequest.kt` — `@NotBlank(name)` Bean Validation 추가
- [x] `ReminderListController.kt`, `ReminderController.kt` — `@Valid` 어노테이션 적용
- [x] `GlobalExceptionHandler.kt` — `MethodArgumentNotValidException` 핸들러 추가 (→ 400)

---

## 🟡 중기 개선 (설계 / 성능)

### 상태 관리 설계
- [ ] `AppContext.tsx` — `setLists`, `setReminders` 외부 노출 제거 → `reorderLists`/`reorderReminders` 내부에서 처리
- [ ] `AppContext.tsx` — list/reminder 도메인 Context로 분리 검토

### 불필요한 재조회 제거
- [x] `AppContext.tsx` — `deleteReminder()` 후 `getLists()` 재조회 → `reminderCount` 로컬 -1 업데이트로 대체
- [x] `AppContext.tsx` — `toggleComplete()` 후 `getLists()` 재조회 → `reminderCount` 로컬 업데이트로 대체
- [x] `AppContext.tsx` — `createReminder()` 후 `getLists()` 재조회 → `reminderCount` 로컬 +1 업데이트로 대체

### useCallback 의존성 최적화
- [ ] `AppContext.tsx` — `deleteList` / `deleteReminder` 의존성 배열에서 `selectedId`/`selectedReminderId` 제거 (`useRef` 또는 함수형 업데이트로 대체)

### 타입 안전성
- [ ] `types/index.ts` — `SelectedId` 타입 재설계: `number | SmartFilter | null`에서 `SmartFilter`를 string literal union으로 명확히 분리
- [ ] `AppContext.tsx` — `as SmartFilter` 캐스팅 제거, 타입 가드 함수로 대체

### 중복 코드
- [ ] `domain/list/ReorderRequest.kt`, `domain/reminder/ReorderRequest.kt` — 공통 위치로 통합 또는 단일 파일로 병합

---

## 🔵 장기 개선 (확장성 / 품질)

### 성능
- [ ] `ReminderListController.kt`, `ReminderController.kt` — 페이지네이션 파라미터 추가 (`page`, `size`)
- [ ] `DefaultReminderService.kt` — `getByFilter()` 결과에 페이지네이션 적용

### 테스트
- [ ] 프론트엔드 — `api.ts` 단위 테스트 추가 (fetch mock)
- [ ] 프론트엔드 — `AppContext` 테스트 추가 (낙관적 업데이트 롤백 시나리오 포함)
- [ ] 백엔드 — 잘못된 reorder IDs 에러 케이스 테스트 추가
- [ ] 백엔드 — 존재하지 않는 ID 경계 케이스 테스트 보강

### 설정 / 환경
- [ ] `api.ts` — BASE_URL을 환경변수(`NEXT_PUBLIC_API_URL`)로 분리
- [ ] `application.properties` — 프로파일 분리 (`dev` / `prod`), H2 콘솔은 dev 전용으로 제한
