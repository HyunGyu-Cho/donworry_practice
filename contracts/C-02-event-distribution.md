# C-02 — 업무별 이벤트 분배

> **C-02 란** P1 이 받은 CakeShop 이벤트를 보안 탐지(P2)와 예상 정산(P5)에
> 나눠 주는 약속이다. 번호 체계는 [계약 지도](README.md#번호가-무슨-뜻인가) 참고.

- **방향** P1 `integration` → P2 `risk` · P5 `settlement`
- **방식** Spring Modulith 내부 비동기 이벤트 + Event Publication Registry
- **검증** T-01(중복 접수) · T-02(P2·P5 독립성) · T-14(CakeShop 비차단)
- **상태** **채택**

핵심은 **P2 와 P5 가 서로를 모른다**는 것이다. 한쪽이 실패해도 다른 쪽의 성공은
되돌아가지 않는다. 그래서 둘을 직접 호출로 잇지 않고 이벤트로 나눈다.

---

## 1. 이벤트 데이터

| 필드 | 뜻 |
|---|---|
| `eventId` | P1 이 저장한 원천 이벤트 ID |
| `tenantId` | 그 이벤트가 속한 고객사 |
| `schemaVersion` | 해석할 원천 이벤트 버전 |

이벤트 종류와 상세 자료는 [C-01](C-01-cakeshop-event.md)(CakeShop 이벤트 수신)의
원천 이벤트 계약을 따른다. 타입은 [공통 필드](common-fields.md) 를 따른다.

## 2. 전달과 처리

- P1 은 **원천 이벤트 저장과 내부 이벤트 발행 기록을 하나의 DB 트랜잭션**으로 처리한다.
- **커밋 이후** P2 와 P5 가 각자 별도의 작업으로 처리한다.
- Event Publication Registry 가 **리스너별** 전달·처리 기록을 저장한다.
  한쪽이 실패해도 다른 쪽의 성공은 유지된다.
- P2 는 로그인·계정·거래 위험 탐지에, P5 는 확정 결제·취소·환불의 예상 정산 반영에 쓴다.
- 각 소비 모듈은 `eventId` 와 업무 식별자에 **DB 제약**을 걸어 같은 이벤트의
  중복 결과를 막는다. **Registry 가 업무 멱등성을 대신 보장하지 않는다.**
- `consumerCode` 를 이벤트가 매번 지정해야 하는 입력값으로 강제하지 않는다.
  수신 리스너 식별과 업무 상태는 구분한다.

## 3. 재시도 (추천안)

단일 Spring Boot 인스턴스에서 **1 분 간격**으로 오래된 미완료 이벤트를 **최대 20 건**
확인·재처리한다. **5 분 이상** 미완료된 항목이 대상이고, 반복 실패(예: 5 회)는
관리자가 점검한다.

채택한 Modulith 버전이 재처리·횟수를 지원하는지는 **구현 테스트로 확인한다.**

## 4. 저장 방식 (추천안 → 구현 반영됨)

Spring Modulith JDBC Registry + PostgreSQL 을 쓰고, **테이블은 Flyway 로 관리**한다.
Registry 를 추가했다고 자동 무제한 재시도가 보장되는 것은 아니다.

> 이미 저장소에 반영되어 있다. `spring-modulith-starter-jdbc` 를 쓰고,
> Registry 기술 테이블은 `db/migration/V20261016_104659__registry_event_publication.sql`
> 이 만든다. `schema-initialization.enabled` 는 `false` 다.
