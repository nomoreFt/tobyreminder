# PRD: TobyReminder - Apple Reminders Web Clone

## 1. 개요

Apple Reminders 앱의 핵심 기능을 웹에서 사용할 수 있는 서비스.
심플하고 직관적인 UX를 유지하면서 브라우저에서 동작하는 리마인더 관리 도구.

---

## 2. 목표

- Apple Reminders의 핵심 UX(목록 + 리마인더 + 스마트 목록)를 웹으로 구현
- REST API 기반 백엔드와 Next.js 프론트엔드의 풀스택 구조 학습 및 시연
- H2 인메모리 DB로 빠른 프로토타이핑, 추후 PostgreSQL 전환 가능한 구조

## 3. 범위 (MVP)

### In Scope
- 리마인더 목록(List) CRUD
- 리마인더(Reminder) CRUD
- 완료 처리 (toggle)
- 중요도(Priority) 설정
- 마감일/시간(Due Date) 설정
- 플래그(Flag) 설정
- 스마트 목록: 오늘(Today), 예정(Scheduled), 전체(All), 플래그됨(Flagged), 완료됨(Completed)
- 리마인더 메모(Notes)

### Out of Scope (v1 이후)
- 사용자 인증 / 다중 사용자
- 알림 / 푸시 알림
- 하위 리마인더 (Sub-tasks)
- 반복 일정 (Recurrence)
- 태그
- 파일 첨부
- 협업 / 공유 목록

---

## 4. 사용자 시나리오

### 시나리오 1: 새 목록 만들기
1. 사이드바 하단 "목록 추가" 클릭
2. 목록 이름 입력, 색상 선택
3. 목록이 사이드바에 추가됨

### 시나리오 2: 리마인더 추가
1. 목록 선택 후 "+" 버튼 또는 엔터로 리마인더 추가
2. 제목 입력
3. 상세 패널에서 마감일, 메모, 우선순위, 플래그 설정

### 시나리오 3: 스마트 목록 보기
1. 사이드바 상단 "오늘" 클릭
2. 오늘 마감인 모든 리마인더를 목록 구분 없이 표시

### 시나리오 4: 완료 처리
1. 리마인더 왼쪽 체크 버튼 클릭
2. 완료 시 취소선 + 흐림 처리, "완료됨" 스마트 목록으로 이동

---

## 5. 기능 요구사항

### 5.1 리마인더 목록 (ReminderList)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 목록 이름 |
| color | String | HEX 색상 코드 (예: #FF3B30) |
| icon | String | SF Symbol 이름 또는 이모지 |
| sortOrder | Int | 정렬 순서 |
| createdAt | DateTime | 생성일 |

**API**
- `GET /api/lists` - 전체 목록 조회
- `POST /api/lists` - 목록 생성
- `PUT /api/lists/{id}` - 목록 수정
- `DELETE /api/lists/{id}` - 목록 삭제 (소속 리마인더도 삭제)

### 5.2 리마인더 (Reminder)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| listId | Long | FK → ReminderList |
| title | String | 제목 (필수) |
| notes | String? | 메모 |
| dueDate | LocalDate? | 마감일 |
| dueTime | LocalTime? | 마감 시간 |
| priority | Enum | NONE / LOW / MEDIUM / HIGH |
| isFlagged | Boolean | 플래그 여부 |
| isCompleted | Boolean | 완료 여부 |
| completedAt | DateTime? | 완료 처리 시각 |
| sortOrder | Int | 목록 내 정렬 순서 |
| createdAt | DateTime | 생성일 |

**API**
- `GET /api/lists/{listId}/reminders` - 목록별 리마인더 조회
- `GET /api/reminders?filter={today|scheduled|all|flagged|completed}` - 스마트 목록
- `POST /api/lists/{listId}/reminders` - 리마인더 생성
- `PUT /api/reminders/{id}` - 리마인더 수정
- `PATCH /api/reminders/{id}/complete` - 완료 토글
- `DELETE /api/reminders/{id}` - 리마인더 삭제

### 5.3 스마트 목록 필터 로직

| 필터 | 조건 |
|------|------|
| Today | dueDate = 오늘 AND isCompleted = false |
| Scheduled | dueDate IS NOT NULL AND isCompleted = false |
| All | isCompleted = false |
| Flagged | isFlagged = true AND isCompleted = false |
| Completed | isCompleted = true |

---

## 6. 기술 스택

### Backend
| 항목 | 기술 |
|------|------|
| Framework | Spring Boot 4.0.3 |
| Language | Kotlin |
| ORM | Spring Data JPA |
| DB | H2 (in-memory, dev) |
| Build | Gradle Kotlin DSL |
| API Style | REST (JSON) |
| Port | 8080 |

### Frontend
| 항목 | 기술 |
|------|------|
| Framework | Next.js (latest, App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS |
| HTTP Client | fetch / axios |
| State | React Context + useState |
| Port | 3000 |

---

## 7. UI 레이아웃

```
┌─────────────────────────────────────────────────────────┐
│  TobyReminder                                           │
├──────────────┬──────────────────────┬───────────────────┤
│  사이드바     │  리마인더 목록        │  상세 패널         │
│              │                      │                   │
│ [스마트목록]  │  ○ 할 일 제목         │  제목             │
│  오늘    3   │  ○ 할 일 제목 2       │  ─────────────   │
│  예정    5   │  ● 완료된 항목        │  메모             │
│  전체    12  │                      │  ─────────────   │
│  플래그   2  │  + 새 리마인더 추가   │  마감일  [날짜]   │
│  완료됨  8   │                      │  우선순위 [선택]  │
│              │                      │  플래그   [토글]  │
│ ─────────── │                      │                   │
│ [내 목록]    │                      │                   │
│  🔴 개인  4  │                      │                   │
│  🔵 업무  7  │                      │                   │
│              │                      │                   │
│  + 목록 추가 │                      │                   │
└──────────────┴──────────────────────┴───────────────────┘
```

---

## 8. 비기능 요구사항

- **응답 속도**: API 응답 200ms 이내 (H2 기준)
- **CORS**: 백엔드에서 `http://localhost:3000` 허용
- **에러 처리**: API 에러 시 프론트에서 toast 알림
- **낙관적 업데이트**: 완료 토글 등 즉각적인 UI 반응

---

## 9. 개발 순서 (제안)

### Phase 1 - Backend API
1. Entity 정의 (ReminderList, Reminder)
2. Repository, Service, Controller 구현
3. CORS 설정
4. API 수동 테스트 (curl / HTTP Client)

### Phase 2 - Frontend 기반
1. Next.js 프로젝트 생성
2. 레이아웃 컴포넌트 (사이드바, 메인, 상세 패널)
3. API 클라이언트 모듈

### Phase 3 - 기능 연결
1. 목록 CRUD UI
2. 리마인더 CRUD UI
3. 스마트 목록 필터
4. 완료 토글 + 즉시 숨김 + 낙관적 업데이트
5. 드래그 앤 드롭 정렬 (dnd-kit)

---

## 10. 확정 결정 사항

| 항목 | 결정 |
|------|------|
| 목록 삭제 시 리마인더 처리 | 소속 리마인더 **함께 삭제** (Cascade) |
| 완료된 리마인더 표시 | **즉시 숨김** (목록에서 즉각 제거, 완료됨 스마트 목록에서만 확인) |
| DB | **H2 인메모리** 유지 |
| 정렬 | **드래그 앤 드롭** 지원 (sortOrder 필드 활용) |
| 프론트 상태 관리 | **React Context** |
| UI/UX | **Apple Reminders 최대한 유사하게** (색상, 폰트, 레이아웃, 인터랙션) |

### Apple Reminders UI 레퍼런스
- 배경: 흰색/밝은 회색 (macOS 스타일)
- 사이드바: 반투명 frosted glass 느낌, 목록별 컬러 아이콘
- 스마트 목록: 파란 배지 숫자, 격자형 카드 배치 (Today/Scheduled/All/Flagged)
- 체크 버튼: 원형, 목록 색상으로 채워짐
- 상세 패널: 오른쪽 슬라이드 인, 인풋 스타일 최소화
- 폰트: SF Pro 계열 (웹: -apple-system, BlinkMacSystemFont)
- 드래그: 아이템 간 순서 변경 시 시각적 플레이스홀더 표시
