# TobyReminder Task List

> plan.md 기반 세부 작업 체크리스트.
> 완료된 작업은 `- [x]`로 표시.

---

## Phase 1 — Backend 기반 구조

> 목표: DB 스키마 + REST API 골격 완성. curl로 동작 확인.

### 1-1. 패키지 구조 셋업
- [x] `domain/list/` 패키지 디렉토리 생성
- [x] `domain/reminder/` 패키지 디렉토리 생성
- [ ] `config/` 패키지 디렉토리 생성

### 1-2. Entity & Enum
- [x] `Priority.kt` — `NONE / LOW / MEDIUM / HIGH` enum 작성
- [x] `ReminderList.kt` — `@Entity`, 연관관계 제거, `update()` 메서드 포함
- [x] `Reminder.kt` — `@Entity`, `listId: Long` (연관관계 대신 ID 참조), `Priority` enum 컬럼 포함
- [x] `ReminderListTest.kt` — 생성자/update()/createdAt 자동등록 테스트 7개 (모두 PASS)

### 1-3. DTO
- [x] `ReminderListRequest.kt` — 생성/수정용 request DTO (name, color, icon)
- [x] `ReminderListResponse.kt` — 응답 DTO (id, name, color, icon, sortOrder, reminderCount, createdAt)
- [ ] `ReminderRequest.kt` — 생성/수정용 request DTO (title, notes, dueDate, dueTime, priority, isFlagged)
- [ ] `ReminderResponse.kt` — 응답 DTO (전체 필드)
- [x] `ReorderRequest.kt` — 순서 변경용 DTO (`ids: List<Long>`)

### 1-4. Repository
- [x] `ReminderListRepository.kt` — `JpaRepository<ReminderList, Long>`, `findAllByOrderBySortOrderAsc()`
- [x] `ReminderRepository.kt` — 기본 CRUD + 스마트 목록 쿼리 메서드
  - [x] `findByListIdAndIsCompletedFalseOrderBySortOrderAsc(listId)`
  - [x] `findByDueDateAndIsCompletedFalse(date)` — Today
  - [x] `findByDueDateIsNotNullAndIsCompletedFalse()` — Scheduled
  - [x] `findByIsCompletedFalse()` — All
  - [x] `findByIsFlaggedTrueAndIsCompletedFalse()` — Flagged
  - [x] `findByIsCompletedTrue()` — Completed

### 1-5. Service
- [x] `ReminderListService.kt`
  - [x] `getLists()` — 전체 목록 조회
  - [x] `createList(request)` — 생성, sortOrder = 현재 max + 1
  - [x] `updateList(id, request)` — 이름/색상/아이콘 수정
  - [x] `deleteList(id)` — 리마인더 먼저 삭제 후 목록 삭제 (Service 명시적 처리)
  - [x] `reorderLists(ids)` — sortOrder 일괄 업데이트
- [x] `ReminderListServiceTest.kt` — 11개 테스트 (모두 PASS)
- [ ] `ReminderService.kt`
  - [ ] `getRemindersByList(listId)` — 목록별 조회
  - [ ] `getByFilter(filter)` — 스마트 목록 필터 분기
  - [ ] `createReminder(listId, request)` — 생성
  - [ ] `updateReminder(id, request)` — 수정
  - [ ] `toggleComplete(id)` — `isCompleted` 반전, `completedAt` 기록
  - [ ] `deleteReminder(id)` — 삭제
  - [ ] `reorderReminders(listId, ids)` — sortOrder 일괄 업데이트

### 1-6. Controller
- [ ] `ReminderListController.kt`
  - [ ] `GET /api/lists`
  - [ ] `POST /api/lists`
  - [ ] `PUT /api/lists/{id}`
  - [ ] `DELETE /api/lists/{id}`
  - [ ] `PATCH /api/lists/reorder`
- [ ] `ReminderController.kt`
  - [ ] `GET /api/lists/{listId}/reminders`
  - [ ] `POST /api/lists/{listId}/reminders`
  - [ ] `PUT /api/reminders/{id}`
  - [ ] `PATCH /api/reminders/{id}/complete`
  - [ ] `DELETE /api/reminders/{id}`
  - [ ] `PATCH /api/lists/{listId}/reminders/reorder`
  - [ ] `GET /api/reminders?filter={today|scheduled|all|flagged|completed}`

### 1-7. 설정
- [ ] `WebConfig.kt` — CORS (`/api/**`, origin `http://localhost:3000`, 5개 메서드)
- [ ] `application.properties` — Jackson 날짜 직렬화 설정 (`spring.jackson.serialization.write-dates-as-timestamps=false`)

### 1-8. 검증
- [ ] `./gradlew build` 통과 확인
- [ ] curl로 목록 생성 → 조회 → 수정 → 삭제 테스트
- [ ] curl로 리마인더 생성 → 완료 토글 → 스마트 목록 필터 테스트

---

## Phase 2 — Frontend 기반 구조

> 목표: Next.js 앱 골격 + Apple Reminders 스타일 정적 UI.

### 2-1. 프로젝트 생성
- [ ] `npx create-next-app@latest frontend` (TypeScript, Tailwind, App Router, src-dir)
- [ ] `frontend/` 디렉토리 구조 확인

### 2-2. 타입 정의
- [ ] `src/types/index.ts` — `ReminderList`, `Reminder`, `Priority`, `SmartFilter` 타입 정의

### 2-3. API 클라이언트
- [ ] `src/lib/api.ts` — fetch 기반 API 클라이언트
  - [ ] `getLists()`
  - [ ] `createList(data)`, `updateList(id, data)`, `deleteList(id)`
  - [ ] `reorderLists(ids)`
  - [ ] `getRemindersByList(listId)`
  - [ ] `getRemindersByFilter(filter)`
  - [ ] `createReminder(listId, data)`, `updateReminder(id, data)`, `deleteReminder(id)`
  - [ ] `toggleComplete(id)`
  - [ ] `reorderReminders(listId, ids)`

### 2-4. Context
- [ ] `src/context/AppContext.tsx` — 전역 상태 + Provider
  - [ ] `lists`, `reminders` 상태
  - [ ] `selectedListId` (number | SmartFilter | null)
  - [ ] `selectedReminderId` (number | null)
  - [ ] 모든 action 함수 (fetchLists, createList, … reorderReminders)
  - [ ] `useApp()` 커스텀 훅

### 2-5. 레이아웃 & 기본 UI
- [ ] `src/app/layout.tsx` — `-apple-system` 폰트, `#F2F2F7` 배경, AppContext Provider 주입
- [ ] `src/app/page.tsx` — 3-컬럼 레이아웃 (Sidebar / ReminderList / DetailPanel)
- [ ] `src/components/ui/CircleCheckbox.tsx` — 원형 체크버튼 (색상 prop, 완료 시 fill)
- [ ] `src/components/ui/ColorPicker.tsx` — Apple 팔레트 8색 선택기

### 2-6. 사이드바 컴포넌트 (정적)
- [ ] `src/components/sidebar/SmartLists.tsx` — 2×2 카드 그리드 (오늘/예정/전체/플래그됨), 배지 숫자
- [ ] `src/components/sidebar/ListItem.tsx` — 컬러 아이콘 + 이름 + 미완료 카운트
- [ ] `src/components/sidebar/Sidebar.tsx` — SmartLists + 내 목록 섹션 + "목록 추가" 버튼

### 2-7. 메인 & 상세 컴포넌트 (정적)
- [ ] `src/components/reminder/ReminderItem.tsx` — CircleCheckbox + 제목 + 마감일 + 우선순위/플래그 아이콘
- [ ] `src/components/reminder/ReminderList.tsx` — 헤더(목록 이름 + 색상) + ReminderItem 목록 + 새 항목 입력 행
- [ ] `src/components/reminder/DetailPanel.tsx` — 제목/메모 입력, 마감일 datepicker, 우선순위 선택, 플래그 토글

---

## Phase 3 — 목록(List) CRUD 연동

> 목표: 사이드바 목록이 실제 API와 연동.

- [ ] 앱 초기 로드 시 `fetchLists()` 호출 → 사이드바 렌더링
- [ ] 스마트 목록 카드 배지 숫자 API 연동 (미완료 리마인더 수)
- [ ] "목록 추가" 클릭 → 인라인 이름 입력 + ColorPicker → `createList()` 호출
- [ ] 목록 이름 더블클릭 → 인라인 편집 모드 → blur/Enter 시 `updateList()` 호출
- [ ] 목록 hover 시 삭제 버튼 노출 → 클릭 시 확인 없이 `deleteList()` 호출
- [ ] 목록 선택 시 `selectedListId` 업데이트 → 중앙 패널 전환

---

## Phase 4 — 리마인더 CRUD 연동

> 목표: 리마인더 추가/수정/삭제/완료가 실제 API와 연동.

- [ ] 목록 선택 → `fetchReminders(listId)` → ReminderItem 렌더링
- [ ] 목록 하단 빈 영역 클릭 또는 `+` 버튼 → 인라인 입력 행 표시 → Enter 시 `createReminder()` 호출
- [ ] ReminderItem 클릭 → `selectedReminderId` 업데이트 → DetailPanel 슬라이드 인
- [ ] DetailPanel 제목 편집 → blur 시 `updateReminder()` 호출
- [ ] DetailPanel 메모 편집 → blur 시 `updateReminder()` 호출
- [ ] DetailPanel 마감일 변경 → `updateReminder()` 호출
- [ ] DetailPanel 우선순위 선택 → `updateReminder()` 호출
- [ ] DetailPanel 플래그 토글 → `updateReminder()` 호출
- [ ] CircleCheckbox 클릭 → 낙관적으로 즉시 목록에서 제거 → `toggleComplete()` 호출
- [ ] ReminderItem hover 시 삭제 버튼 노출 → `deleteReminder()` 호출

---

## Phase 5 — 스마트 목록

> 목표: 사이드바 상단 카드 클릭 시 필터된 리마인더 표시.

- [ ] 오늘 카드 클릭 → `getRemindersByFilter('today')` → 중앙 패널 렌더링
- [ ] 예정 카드 클릭 → `getRemindersByFilter('scheduled')`
- [ ] 전체 카드 클릭 → `getRemindersByFilter('all')`
- [ ] 플래그됨 카드 클릭 → `getRemindersByFilter('flagged')`
- [ ] 완료됨 카드 클릭 → `getRemindersByFilter('completed')` → 목록 이름 그룹 헤더로 구분 표시
- [ ] 스마트 목록 뷰에서도 CircleCheckbox 완료 토글 동작
- [ ] 스마트 목록 뷰에서도 DetailPanel 편집 동작
- [ ] 스마트 목록 뷰 헤더에 필터 이름 + 아이콘 표시

---

## Phase 6 — 드래그 앤 드롭 정렬

> 목표: 리마인더 및 목록 순서를 드래그로 변경, sortOrder 서버 동기화.

### 6-1. 패키지 설치
- [ ] `npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities`

### 6-2. 리마인더 정렬
- [ ] `ReminderList.tsx`에 `SortableContext` + `DndContext` 적용
- [ ] `ReminderItem.tsx`에 `useSortable` 훅 적용 (드래그 핸들 아이콘)
- [ ] 드래그 중 고스트(ghost) 아이템 스타일 적용 (반투명)
- [ ] 드래그 완료(`onDragEnd`) 시 로컬 상태 즉시 업데이트 (낙관적)
- [ ] `reorderReminders(listId, newOrderIds)` 호출 → `PATCH /api/lists/{listId}/reminders/reorder`

### 6-3. 목록 정렬
- [ ] `Sidebar.tsx` 내 목록 섹션에 `SortableContext` + `DndContext` 적용
- [ ] `ListItem.tsx`에 `useSortable` 훅 적용
- [ ] 드래그 완료 시 로컬 상태 즉시 업데이트
- [ ] `reorderLists(newOrderIds)` 호출 → `PATCH /api/lists/reorder`
