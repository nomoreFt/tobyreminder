# TobyReminder 개발 계획 (plan.md)

> spec.md 기반. 단순한 것부터 점진적으로 기능을 추가하는 방식으로 phase를 구성.

---

## 기술 스택 요약

### Backend
| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Spring Boot 4.0.3 | |
| Language | Kotlin | data class로 엔티티 작성 |
| ORM | Spring Data JPA | Hibernate 6 |
| DB | H2 in-memory | 재시작 시 초기화 |
| Build | Gradle Kotlin DSL | |
| API | REST / JSON | Jackson |
| Port | 8080 | |

### Frontend
| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Next.js latest | App Router |
| Language | TypeScript | strict mode |
| Styling | Tailwind CSS v4 | -apple-system 폰트 |
| State | React Context + useState | 전역 목록/리마인더 상태 |
| HTTP | fetch (native) | API client 모듈로 추상화 |
| Drag & Drop | dnd-kit | sortOrder 동기화 |
| Port | 3000 | |

---

## Phase 1 — Backend 기반 구조

> 목표: DB 스키마 + REST API 골격 완성. 프론트 없이 curl로 동작 확인.

### 1-1. 프로젝트 패키지 구조 정의
```
src/main/kotlin/toby/ai/tobyreminder/
├── domain/
│   ├── list/
│   │   ├── ReminderList.kt          # Entity
│   │   ├── ReminderListRepository.kt
│   │   ├── ReminderListService.kt
│   │   └── ReminderListController.kt
│   └── reminder/
│       ├── Reminder.kt              # Entity
│       ├── Priority.kt              # Enum
│       ├── ReminderRepository.kt
│       ├── ReminderService.kt
│       └── ReminderController.kt
└── config/
    └── WebConfig.kt                 # CORS
```

### 1-2. Entity 정의

**ReminderList**
```kotlin
@Entity
class ReminderList(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var name: String,
    var color: String = "#007AFF",   // Apple blue 기본값
    var icon: String = "list.bullet",
    var sortOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @OneToMany(mappedBy = "list", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reminders: MutableList<Reminder> = mutableListOf()
)
```

**Reminder**
```kotlin
@Entity
class Reminder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id")
    var list: ReminderList,
    var title: String,
    var notes: String? = null,
    var dueDate: LocalDate? = null,
    var dueTime: LocalTime? = null,
    @Enumerated(EnumType.STRING)
    var priority: Priority = Priority.NONE,
    var isFlagged: Boolean = false,
    var isCompleted: Boolean = false,
    var completedAt: LocalDateTime? = null,
    var sortOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class Priority { NONE, LOW, MEDIUM, HIGH }
```

### 1-3. API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | /api/lists | 전체 목록 조회 (sortOrder 순) |
| POST | /api/lists | 목록 생성 |
| PUT | /api/lists/{id} | 목록 수정 (이름/색상/아이콘) |
| DELETE | /api/lists/{id} | 목록 + 소속 리마인더 삭제 |
| PATCH | /api/lists/reorder | 목록 순서 일괄 변경 |
| GET | /api/lists/{listId}/reminders | 목록별 리마인더 조회 |
| POST | /api/lists/{listId}/reminders | 리마인더 생성 |
| PUT | /api/reminders/{id} | 리마인더 수정 |
| PATCH | /api/reminders/{id}/complete | 완료 토글 |
| DELETE | /api/reminders/{id} | 리마인더 삭제 |
| PATCH | /api/lists/{listId}/reminders/reorder | 리마인더 순서 일괄 변경 |
| GET | /api/reminders?filter= | 스마트 목록 (today/scheduled/all/flagged/completed) |

### 1-4. CORS 설정
```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
    }
}
```

### 1-5. 검증
```bash
# 목록 생성
curl -X POST http://localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -d '{"name":"개인","color":"#FF3B30"}'

# 리마인더 생성
curl -X POST http://localhost:8080/api/lists/1/reminders \
  -H "Content-Type: application/json" \
  -d '{"title":"장보기","dueDate":"2026-03-11"}'
```

---

## Phase 2 — Frontend 기반 구조

> 목표: Next.js 앱 골격 + Apple Reminders 스타일 레이아웃 + API 연동 없는 정적 UI.

### 2-1. 프로젝트 생성
```bash
cd /Users/nuua/IdeaProjects/sandbox/tobyreminder
npx create-next-app@latest frontend \
  --typescript --tailwind --app --src-dir \
  --no-eslint --import-alias "@/*"
```

### 2-2. 프론트엔드 디렉토리 구조
```
frontend/src/
├── app/
│   ├── layout.tsx           # 루트 레이아웃 (폰트, 배경)
│   └── page.tsx             # 메인 페이지
├── components/
│   ├── sidebar/
│   │   ├── Sidebar.tsx      # 사이드바 전체
│   │   ├── SmartLists.tsx   # 오늘/예정/전체/플래그됨 카드 그리드
│   │   └── ListItem.tsx     # 내 목록 아이템
│   ├── reminder/
│   │   ├── ReminderList.tsx # 중앙 리마인더 목록
│   │   ├── ReminderItem.tsx # 개별 리마인더 행
│   │   └── DetailPanel.tsx  # 우측 상세 편집 패널
│   └── ui/
│       ├── CircleCheckbox.tsx  # 원형 체크버튼
│       └── ColorPicker.tsx     # 색상 선택기
├── context/
│   └── AppContext.tsx        # 전역 상태 (lists, reminders, selectedList, selectedReminder)
├── lib/
│   └── api.ts               # fetch 기반 API 클라이언트
└── types/
    └── index.ts             # ReminderList, Reminder, Priority 타입
```

### 2-3. Apple Reminders 스타일 가이드
| 요소 | 스타일 |
|------|------|
| 폰트 | `-apple-system, BlinkMacSystemFont, "SF Pro Text"` |
| 배경 | `#F2F2F7` (iOS systemGroupedBackground) |
| 사이드바 | `#FFFFFF` + `backdrop-blur`, border-right `#E5E5EA` |
| 스마트 목록 카드 | `2×2` 격자, 둥근 모서리 (`rounded-2xl`), 색상별 아이콘 |
| 체크 버튼 | 원형 border, 완료 시 목록 색상으로 fill + checkmark |
| 선택된 목록 | 좌측 색상 bar 또는 배경 하이라이트 |
| 상세 패널 | 오른쪽에서 슬라이드 인, 배경 흰색 |
| 우선순위 | `!` 아이콘 (LOW=파랑, MEDIUM=주황, HIGH=빨강) |
| 플래그 | `🚩` 아이콘, 활성 시 주황색 |

### 2-4. Context 구조
```typescript
interface AppState {
  lists: ReminderList[]
  reminders: Reminder[]           // 현재 선택된 뷰의 리마인더
  selectedListId: number | SmartFilter | null
  selectedReminderId: number | null
  // actions
  fetchLists: () => Promise<void>
  createList: (data) => Promise<void>
  updateList: (id, data) => Promise<void>
  deleteList: (id) => Promise<void>
  fetchReminders: (listId | filter) => Promise<void>
  createReminder: (listId, data) => Promise<void>
  updateReminder: (id, data) => Promise<void>
  toggleComplete: (id) => Promise<void>   // 낙관적 업데이트
  deleteReminder: (id) => Promise<void>
  reorderReminders: (listId, orderedIds) => Promise<void>
}
```

---

## Phase 3 — 목록(List) CRUD 연동

> 목표: 사이드바에서 목록 생성/수정/삭제가 실제 API와 연동.

### 구현 항목
- [ ] `GET /api/lists` → 사이드바 목록 렌더링
- [ ] "목록 추가" 버튼 → 인라인 입력 + 색상 선택 → `POST /api/lists`
- [ ] 목록 이름 더블클릭 → 인라인 편집 → `PUT /api/lists/{id}`
- [ ] 우클릭 컨텍스트 메뉴 또는 hover 삭제 버튼 → `DELETE /api/lists/{id}`
- [ ] 사이드바 스마트 목록 카드 배지 숫자 (미완료 리마인더 수)

---

## Phase 4 — 리마인더 CRUD 연동

> 목표: 리마인더 추가/수정/삭제/완료 토글이 실제 API와 연동.

### 구현 항목
- [ ] 목록 선택 → `GET /api/lists/{id}/reminders` → 중앙 패널 렌더링
- [ ] `+` 버튼 또는 엔터 → 인라인 새 리마인더 입력 → `POST`
- [ ] 리마인더 클릭 → 우측 상세 패널 열림
- [ ] 상세 패널: 제목/메모/마감일/우선순위/플래그 편집 → `PUT /api/reminders/{id}`
- [ ] 체크 버튼 클릭 → 낙관적으로 즉시 목록에서 제거 → `PATCH .../complete`
- [ ] 스와이프/hover 삭제 → `DELETE /api/reminders/{id}`

---

## Phase 5 — 스마트 목록

> 목표: 사이드바 상단 스마트 목록 카드 클릭 시 필터된 리마인더 표시.

### 구현 항목
- [ ] `GET /api/reminders?filter=today` → 오늘 카드
- [ ] `GET /api/reminders?filter=scheduled` → 예정 카드
- [ ] `GET /api/reminders?filter=all` → 전체 카드
- [ ] `GET /api/reminders?filter=flagged` → 플래그됨 카드
- [ ] `GET /api/reminders?filter=completed` → 완료됨 (목록 없이 그룹별 표시)
- [ ] 스마트 목록에서도 완료 토글, 상세 편집 가능

---

## Phase 6 — 드래그 앤 드롭 정렬

> 목표: 리마인더와 목록을 드래그로 순서 변경, `sortOrder` 서버 동기화.

### 기술: dnd-kit
```bash
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
```

### 구현 항목
- [ ] `ReminderItem`에 `useSortable` 적용
- [ ] 드래그 완료 시 `PATCH /api/lists/{listId}/reminders/reorder` 호출
- [ ] `ListItem`(사이드바)에도 `useSortable` 적용
- [ ] 드래그 완료 시 `PATCH /api/lists/reorder` 호출
- [ ] 드래그 중 플레이스홀더(고스트) 시각적 표시

---

## 파일 위치 정리

```
tobyreminder/                        ← git root
├── spec.md                          ← 요구사항 명세
├── plan.md                          ← 개발 계획 (이 파일)
├── build.gradle.kts                 ← Backend (Spring Boot)
├── src/
│   └── main/kotlin/toby/ai/tobyreminder/
│       ├── domain/list/
│       ├── domain/reminder/
│       └── config/
└── frontend/                        ← Next.js 앱
    ├── package.json
    └── src/
        ├── app/
        ├── components/
        ├── context/
        ├── lib/
        └── types/
```

---

## 진행 체크리스트

- [ ] **Phase 1** — Backend Entity + API + CORS
- [ ] **Phase 2** — Next.js 생성 + 레이아웃 + Context 골격
- [ ] **Phase 3** — 목록 CRUD 연동
- [ ] **Phase 4** — 리마인더 CRUD + 완료 토글
- [ ] **Phase 5** — 스마트 목록 필터
- [ ] **Phase 6** — 드래그 앤 드롭 정렬
