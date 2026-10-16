# ARCHITECTURE.md

승인된 구조와 접근 경계의 요약이다. 상세 설계는 Notion 을 따른다.

## 실행 구조

```
React (ADM/SEC/MER)  ──HTTPS──▶  Spring Boot 단일 앱 ──HTTP──▶ FastAPI (AI)
                                        ▲                          │
CakeShop (별도 앱·DB) ──HTTP 이벤트──────┘      내부 증거 조회·결과 ◀┘
```

## 업무 흐름 — 데이터가 흐르는 방향

```
보안   CakeShop → integration → risk → investigation → (AI)
자금   CakeShop → integration → settlement → cashflow
```

## 컴파일 의존 — 누가 누구를 import 하나

**업무 흐름과 반대다.** 이벤트는 받는 쪽이 보내는 쪽을 알기 때문이다.

```
identity  ◀── integration ◀── risk ◀── investigation
                   ▲                        │
                   └────────────────────────┘
          integration ◀── settlement ◀── cashflow
```

| 모듈 | allowedDependencies |
|---|---|
| `identity` | (없음) |
| `integration` | `identity` |
| `risk` | `integration::events` |
| `investigation` | `risk::events`, `risk`, `integration` |
| `settlement` | `integration::events` |
| `cashflow` | `settlement` |

선언은 각 모듈의 `package-info.java` 에 있다. **`risk` 와 `settlement` 사이에 선이 없다** —
보안 흐름과 자금 흐름이 독립이라는 뜻이다.

## 통신 방식

| 구간 | 방식 | 상태 |
|---|---|---|
| CakeShop → integration | HTTP 이벤트 | 채택 |
| integration → risk · settlement | 내부 비동기 이벤트 + Event Publication Registry | 채택 |
| risk → investigation | 내부 비동기 이벤트 | 채택 |
| investigation ↔ FastAPI | HTTP (Job 접수 · 증거 조회 · 결과 통지) | 채택 |
| cashflow → settlement | 동기 공개 Java 조회 API | 업무 계약 채택, 시그니처는 추천안 |

## 접근 금지

- 다른 모듈의 `internal/` 패키지 (controller · service · repository · entity)
- AI 서버가 업무 DB 를 직접 조회하는 것 — 허용된 증거 API 만 쓴다
- 모듈마다 증거 API 를 따로 여는 것 — 권한 게이트는 `investigation` 한 곳이다

## 공통 기술 코드

`AuthContext` 처럼 전 모듈이 쓰는 소량의 기술 타입은 **루트 패키지에 직접** 둔다.
`com.example.platform.security` 같은 하위 패키지를 만들면 Modulith 가 그것을
7번째 업무 모듈로 인식하고, 6개 모듈이 전부 그것을 의존해 경계가 무의미해진다.

## 데이터

- PostgreSQL 17 하나. 모듈마다 자기 테이블만 쓴다.
- 스키마 변경은 Flyway 만 한다. `ddl-auto: validate`.
- Event Publication Registry 기술 테이블도 Flyway 가 만든다.
