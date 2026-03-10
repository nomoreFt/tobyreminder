# CLAUDE.md — TobyReminder 코딩 규칙

이 파일은 Claude Code가 이 프로젝트에서 반드시 따라야 할 코딩 규칙을 정의합니다.

> **관리 원칙**: 사용자가 추가로 요구한 사항은 이 파일에 즉시 반영한다.

---

## 프로젝트 개요

- **스펙**: `spec.md` / **계획**: `plan.md` / **작업 목록**: `tasks.md`

---

## 공통 규칙

### 작업 흐름
- 기능을 구현하거나 수정할 때는 **반드시 테스트를 함께 작성**한다.
- 작업이 완료되면 **`tasks.md`의 해당 항목을 `- [x]`로 체크**한다.
- 새로운 작업이 생기면 `tasks.md`에 추가한다.

### 파일 생성 원칙
- 기존 파일을 수정할 수 있으면 새 파일을 만들지 않는다.
- 한 번만 쓰이는 로직을 위한 헬퍼/추상화를 만들지 않는다.

---

## Backend (Kotlin / Spring Boot)

### 패키지 구조
```
src/main/kotlin/toby/ai/tobyreminder/
├── domain/
│   ├── list/        # ReminderList 도메인
│   └── reminder/    # Reminder 도메인
└── config/          # CORS 등 설정
```
- 도메인별로 Entity / Repository / Service / Controller를 같은 패키지에 둔다.

### Entity
- **JPA 연관관계(`@OneToMany`, `@ManyToOne` 등)를 가급적 사용하지 않는다.**

### Service
- **Service 인터페이스는 `domain/{도메인}/ports/in/` 패키지에 둔다.**
- **구현체 네이밍은 `Default` 접두사를 사용한다. (예: `DefaultReminderListService`)**
- `@Service`, `@Transactional`은 구현체에만 붙인다.
- 주입 및 테스트는 인터페이스 타입으로 받는다.

---

## Backend 테스트

### 원칙
- **Service 테스트는 `@SpringBootTest` 통합 테스트로 작성한다. Mock 사용 금지.**
- 각 테스트는 `@Transactional`로 감싸 DB를 자동 롤백한다.
- Entity 단위 테스트는 Spring Context 없이 순수 Kotlin으로 작성한다.
- **Controller 테스트는 `@SpringBootTest` + `MockMvc`(`webAppContextSetup`)로 작성한다.**
  - Spring Boot 4에서 `@WebMvcTest` / `@AutoConfigureMockMvc`가 제거됨.
  - 서비스는 `@MockitoBean`으로 교체한다.

### 스타일
```kotlin
@DisplayName("클래스명 설명")
class XxxTest {

    @Nested
    @DisplayName("메서드명 또는 시나리오")
    inner class MethodName {

        @Test
        @DisplayName("한국어로 무엇을 검증하는지 서술")
        fun `영어 스네이크케이스 메서드명`() { ... }
    }
}
```
- `@DisplayName`은 **한국어**로 작성한다.
- 테스트 파일은 대상 클래스와 같은 패키지 하위에 `XxxTest.kt`로 생성한다.
- AssertJ (`assertThat`) 사용을 기본으로 한다.

---


## Git

- 기능 단위로 커밋한다.
- 커밋 메시지는 영어, 명령형(`Add`, `Fix`, `Update`, `Remove`)으로 작성한다.
