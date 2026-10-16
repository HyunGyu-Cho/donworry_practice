# donworry

쇼핑몰 운영사를 위한 두 가지 서비스. **두 기능은 서로 독립이다.**

- **보안** — 수상한 로그인·거래를 경보로 만들고, 관련 경보를 사건으로 묶고,
  AI 가 증거 보고서를 쓰고, **사람이 최종 판정**한다.
- **자금** — 시작 자금과 예정 지출로 날짜별 예상 잔액과 부족 시점을 보여준다.

Spring Boot 단일 앱(업무 모듈 6개) + FastAPI AI 서버 + React 화면.
CakeShop 은 [별도 저장소](https://github.com/team-sweethan/cakeshop)의 별도 앱이고 HTTP 로만 연동한다.

## 빠르게 돌려보기

```bash
docker compose up -d
```

```bash
cd backend
./gradlew spotlessCheck test
```

**테스트는 DB 없이도 돈다.** 모듈 경계 검사는 Spring 컨텍스트를 띄우지 않기 때문이다.
앱을 띄우려면 컨테이너가 먼저 올라와 있어야 한다 — `./gradlew bootRun`.

## 포트

| | 포트 | |
|---|---|---|
| 이쪽 플랫폼 | **8081** | `bootRun` |
| 플랫폼 DB | **5433** | PostgreSQL 17 컨테이너 (`DB_PORT` 로 변경) |
| CakeShop | 8080 | 별도 저장소 |
| CakeShop DB | 3307 | MariaDB, 로컬 설치 |

호스트 5433 을 쓰는 이유는 [AGENTS.md](AGENTS.md) 의 "자주 걸리는 함정" 에 있다.

## CakeShop 과 함께 띄우기

**개발 대부분은 CakeShop 없이 한다.** 이벤트를 `curl` 로 직접 쏘면 되기 때문이다.
실제로 띄워야 하는 때는 두 번뿐이다 — CakeShop 쪽 전송 코드를 고칠 때와 최종 시연 때.
Toss 테스트 키와 메일·S3 설정이 담긴 `.env` 가 필요하다
([cakeshop 저장소](https://github.com/team-sweethan/cakeshop)의 `.env_sample` 참고).

## 폴더

| 폴더 | 내용 |
|---|---|
| `backend/` | Spring Boot 실행 앱 1개 + Spring Modulith 업무 모듈 6개 |
| `frontend/` | React + TypeScript (아직 없음) |
| `ai-server/` | FastAPI + LangGraph (아직 없음) |
| `contracts/` | 담당자 간 계약 · 공통 필드 · 검증 항목 — **정본** |
| `docs/` | 작업 계획 쓰는 법과 진행 상황 |

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

| 문서 | 내용 |
|---|---|
| [AGENTS.md](AGENTS.md) | 코딩 에이전트와 개발자가 지킬 규칙 · 자주 걸리는 함정 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 모듈 6개 · 의존 방향 · 접근 경계 |
| [contracts/README.md](contracts/README.md) | 계약 지도 (C-01~C-07) |
| [contracts/common-fields.md](contracts/common-fields.md) | 공통 필드의 이름과 타입 |
| [contracts/verification.md](contracts/verification.md) | 합격 기준 T-01~T-15 |
| [contracts/detection-rules.md](contracts/detection-rules.md) | 탐지 규칙을 만드는 틀 |
| [docs/PLANS.md](docs/PLANS.md) | 작업 계획 쓰는 법과 세션 복구 |
| [docs/ROADMAP.md](docs/ROADMAP.md) | **지금 누가 무엇을 하고 있나 · 아직 안 정한 것** |
