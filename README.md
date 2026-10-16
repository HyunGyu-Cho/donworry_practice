# donworry

쇼핑몰 운영사를 위한 두 가지 서비스. **두 기능은 서로 독립이다.**

- **보안** — 수상한 로그인·거래를 경보로 만들고, 관련 경보를 사건으로 묶고,
  AI 가 증거 보고서를 쓰고, **사람이 최종 판정**한다.
- **자금** — 시작 자금과 예정 지출로 날짜별 예상 잔액과 부족 시점을 보여준다.

## 구성

| 폴더 | 내용 |
|---|---|
| `backend/` | Spring Boot 실행 앱 1개 + Spring Modulith 업무 모듈 6개 |
| `frontend/` | React + TypeScript (예정) |
| `ai-server/` | FastAPI + LangGraph (예정) |
| `contracts/` | 담당자 간 계약 C-01~C-07 과 검증 항목 — **정본** |
| `docs/` | 개발 규칙·Execution Plan (예정) |

CakeShop 은 [별도 저장소](https://github.com/team-sweethan/cakeshop)의 별도 앱이다. HTTP 로만 연동한다.

## 시작하기

```bash
# 1. DB 띄우기
docker compose up -d

# 2. 빌드와 검사
cd backend
./gradlew spotlessCheck test
```

테스트는 DB 없이도 돈다. 모듈 경계 검사는 Spring 컨텍스트를 띄우지 않기 때문이다.

## 모듈 경계

| 모듈 | 담당 | 의존 |
|---|---|---|
| `identity` | P1 | (없음) |
| `integration` | P1 | `identity` |
| `risk` | P2 | `integration::events` |
| `investigation` | P3 | `risk::events`, `risk`, `integration` |
| `settlement` | P5 | `integration::events` |
| `cashflow` | P6 | `settlement` |

**`risk` 와 `settlement` 사이에 선이 없다.** 보안 흐름과 자금 흐름은 독립이고,
`allowedDependencies` 와 ArchUnit 이 그것을 빌드로 강제한다.

확인해 보려면 `risk` 안에서 `settlement` 타입을 하나 import 하고 테스트를 돌려 보라.
빨개져야 정상이다.

## DB 변경

파일명을 직접 짓지 않는다. 설명은 **모듈명으로 시작**한다.

```bash
cd backend
./gradlew newMigration -Pdesc=risk_add_alert_table
```

```
src/main/resources/db/migration/V20261016_104659__risk_add_alert_table.sql
```

스키마를 건드리는 도구는 Flyway 하나다. `ddl-auto` 는 `validate` 로 막혀 있다.

## 더 읽을 것

- [AGENTS.md](AGENTS.md) — 코딩 에이전트와 개발자가 지킬 규칙
- [ARCHITECTURE.md](ARCHITECTURE.md) — 구조와 접근 경계
- [contracts/README.md](contracts/README.md) — 계약 지도
