# Code Review: build.gradle.kts

**대상 파일:** `build.gradle.kts`
**리뷰 날짜:** 2026-03-10

---

## 요약

전반적으로 표준적인 Spring Boot + Kotlin + JPA 설정이나, 몇 가지 개선 포인트가 있습니다.

---

## 이슈

### 🔴 Critical

없음

---

### 🟡 Warning

#### 1. Lombok 의존성 불필요 (line 33, 35)
```kotlin
compileOnly("org.projectlombok:lombok")
annotationProcessor("org.projectlombok:lombok")
```
Lombok은 Java 어노테이션 프로세서로, **Kotlin에서 동작하지 않습니다.**
Kotlin에서는 `data class`로 대체되므로 두 줄 모두 제거해야 합니다.

#### 2. 테스트 의존성 오류 (line 36)
```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
```
`spring-boot-starter-data-jpa-test`는 존재하지 않는 아티팩트입니다.
JPA 테스트를 위한 올바른 의존성은:
```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
```

---

### 🔵 Suggestion

#### 3. `description` 필드 기본값 (line 11)
```kotlin
description = "Demo project for Spring Boot"
```
Spring Initializr 기본값 그대로입니다. 실제 프로젝트 설명으로 교체 권장:
```kotlin
description = "AI-powered reminder service"
```

#### 4. `configurations` 블록 불필요 (line 19-23)
```kotlin
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
```
Lombok 제거 시 이 블록도 함께 제거합니다. Kotlin 프로젝트에선 사용처가 없습니다.

---

## 수정 제안 요약

| 라인 | 현재 | 제안 |
|------|------|------|
| 11 | `"Demo project for Spring Boot"` | 실제 설명으로 변경 |
| 19-23 | `configurations { compileOnly { ... } }` | 제거 |
| 33 | `compileOnly("...lombok")` | 제거 |
| 35 | `annotationProcessor("...lombok")` | 제거 |
| 36 | `spring-boot-starter-data-jpa-test` | `spring-boot-starter-test` 로 변경 |
