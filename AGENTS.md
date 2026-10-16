# AGENTS.md

코딩 에이전트가 새 세션에서 가장 먼저 읽는 문서다. 설계를 여기에 복사하지 않는다.
어디를 봐야 하는지만 적는다.

## 이 저장소가 무엇인가

쇼핑몰 운영사를 위한 두 가지 서비스다. **두 기능은 서로 독립이다.**

- **보안** — 수상한 로그인·거래를 경보로 만들고, 관련 경보를 사건으로 묶고, AI 가 증거
  보고서를 쓰고, **사람이 최종 판정**한다.
- **자금** — 시작 자금과 예정 지출로 날짜별 예상 잔액과 부족 시점을 보여준다.

## 실행 단위

- `backend/` — Spring Boot 실행 앱 **1개**. Gradle 프로젝트도 **1개**.
  그 안에 Spring Modulith 업무 모듈 6개가 들어 있다.
- `ai-server/` — FastAPI + LangGraph. **별도 실행**이며 Gradle 모듈이 아니다.
- `frontend/` — React + TypeScript.
- CakeShop 은 **별도 저장소의 별도 애플리케이션**이다. HTTP 로만 연동한다.

## 모듈과 소유자

| 모듈 | 담당 | 하는 일 |
|---|---|---|
| `identity` | P1 | 고객사·사용자·권한 |
| `integration` | P1 | CakeShop 이벤트 수신·검증·분배 |
| `risk` | P2 | 위험 규칙·경보(Alert) |
| `investigation` | P3 | 사건(Case)·판정·AI Job·증거 게이트 |
| `settlement` | P5 | 예상 정산·Mock 대사 |
| `cashflow` | P6 | 시작 자금·예정 지출·잔액 전망 |

P4 는 `ai-server/` 전체를 담당한다.

## 반드시 지킬 것

1. **다른 모듈의 `internal/` 을 참조하지 않는다.** 공개 API·DTO·이벤트만 쓴다.
2. **`allowedDependencies` 에 없는 모듈을 import 하지 않는다.**
   선언은 각 모듈의 `package-info.java` 에 있다. 어기면 `ModularityTests` 가 실패한다.
3. **보안 흐름과 자금 흐름을 섞지 않는다.** `risk`·`investigation` 은 `settlement`·`cashflow` 를
   모르고, 그 반대도 같다. `FlowSeparationTests` 가 검사한다.
4. **스키마는 Flyway 만 건드린다.** `ddl-auto` 는 `validate` 다. 마이그레이션 파일명은
   `./gradlew newMigration -Pdesc=<모듈명>_<설명>` 으로 만든다. 직접 짓지 않는다.
5. **실행하지 않은 테스트를 통과라고 보고하지 않는다.** 테스트를 지우거나
   `continue-on-error` 로 우회하지 않는다.
6. **공개 계약을 바꾸려면 먼저 보고한다.** `contracts/` 의 C-01~C-07 이다.
7. **클래스 이름은 모듈이 달라도 겹치지 않게 짓는다.** Spring 빈 이름의 기본값이
   단순 클래스명이라, `risk.internal.SourceEventListener` 와
   `settlement.internal.SourceEventListener` 가 동시에 있으면 앱이 뜨지 않는다.
   역할이 드러나는 이름을 쓴다 — `AlertDetectionListener`, `TransactionProjectionListener`.

## 번호를 쓸 때

C-xx(계약) · T-xx(테스트) · D-x(결정 대기) · ADR-xxx · FIX-xx 같은 번호는
**처음 나올 때 한 번 풀어서 적는다.** 읽는 사람이 번호를 외우고 있다고 가정하지 않는다.

> C-03 → "C-03(경보를 사건으로 넘기는 약속)"

번호 체계 전체는 [contracts/README.md](contracts/README.md#번호가-무슨-뜻인가) 에 있다.

## 자주 걸리는 함정

- **로컬 PostgreSQL 과 포트 충돌.** PC 에 PostgreSQL 을 설치해 둔 사람은 5432 가 이미
  차 있어, 앱이 컨테이너가 아니라 로컬 DB 에 붙는다. 증상은 "password 인증 실패" 하나뿐이라
  원인을 찾기 어렵다. 그래서 compose 의 호스트 포트 기본값을 **5433** 으로 둔다.
- **공개 인터페이스만 만들고 구현을 안 두면** 주입받는 쪽에서 앱이 뜨지 않는다.
  인터페이스는 모듈 루트에, 구현은 `internal` 에 둔다.

## 멈추고 사람에게 물어야 할 때

- 공개 계약(C-01~C-07)이나 ERD 를 바꿔야 할 때
- 다른 담당자의 소유 데이터·화면을 고쳐야 할 때
- CakeShop 의 실제 이벤트가 설계와 다르고 Mock 으로도 검증이 안 될 때
- 고객사 권한·개인정보 처리에 영향을 주는 변경
- 원인을 모른 채 검증을 우회해야만 진행되는 경우

## 작업 순서

1. 이 문서 → `ARCHITECTURE.md` → `contracts/README.md` 의 계약 지도
   → `contracts/common-fields.md` 의 필드 이름·타입 규칙
2. 담당 계약(C-xx)과 검증 항목(T-xx) 확인
3. 이미 구현된 것과 아직 Mock 인 것을 구분
4. 작업 계획을 `docs/exec-plans/` 에 쓴다. **승인 절차는 없다.** 범위를 적는 것이 목적이다
5. 구현 → 테스트 실행 → 실제 결과를 계획서에 기록 → PR

## 검사 명령

```bash
cd backend
./gradlew spotlessCheck test
```
