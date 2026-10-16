# C-01 — CakeShop 원천 이벤트 수신

> **C-01 이란** 일곱 개 계약 중 첫 번째다. 계약(Contract)은 담당자끼리 주고받을
> 데이터의 약속을 말한다. 번호 체계는 [계약 지도](README.md#번호가-무슨-뜻인가) 참고.

CakeShop(별도 앱·별도 DB)이 플랫폼으로 보내는 이벤트의 약속이다.

- **방향** CakeShop → P1 `integration`
- **방식** 외부 HTTP
- **검증** T-01(중복 수신·ID 충돌) · T-14(플랫폼 장애 시 CakeShop 비차단)
- **상태** 설계는 채택, **원천 신호 조사 결과를 반영해 범위를 다시 합의해야 한다**

---

## 1. 조사 결과 — 2026-10-16

CakeShop 소스를 직접 확인했다. **결론: 지금 CakeShop 은 외부로 아무것도 내보내지 않는다.**
전송 경로 자체를 새로 만들어야 하고, 신호 중 일부는 기록조차 되지 않고 있다.

### 1.1 바로 발행할 수 있는 것

DB 에 데이터가 있고 트랜잭션 경계가 분명해, 그 안에서 전송 대기 기록을 남기면 된다.

| 신호 | 훅 지점 | 원천 ID 후보 | 비고 |
|---|---|---|---|
| 주문 생성 | `OrderService.createGeneralOrder` · `createCartOrder` | `orders.id` | **`orders.request_key` 가 이미 멱등키다.** `(member_id, request_key)` UNIQUE |
| 결제 확정 | `PaymentService.completePayment` | `payments.id`, Toss `paymentKey` | 재고 차감·주문 상태 변경과 **한 트랜잭션**이라 훅 지점으로 안전하다 |
| 취소·환불 확정 | `RefundService.completeCancellation` | `CancellationResult.transactionKey` | Toss 취소 transactionKey 가 원천 ID 로 적합하다 |
| 회원 정지·해제 | `member_status_histories` 적재 지점 | `member_status_histories.id` | `action` 이 `SUSPEND` / `ACTIVATE` 두 개뿐이다 |

### 1.2 지금은 발행할 수 없는 것 — CakeShop 수정이 필요하다

| 신호 | 현황 |
|---|---|
| **로그인 성공** | `RoleBasedAuthenticationSuccessHandler` 가 역할에 따라 리다이렉트만 한다. **기록하지 않는다** |
| **로그인 실패** | `PortalAuthenticationFailureHandler` 가 로그인 화면으로 되돌릴 뿐이다. **기록하지 않는다** |
| 로그아웃 | `PortalLogoutSuccessHandler` 도 같다 |
| 회원가입 | 가입 시점을 남기는 이력이 없다 |
| 탈퇴 | `MemberService.withdraw` 는 있으나 `member_status_histories.action` 에 탈퇴가 없다 |

로그인 이력 테이블도, 마지막 로그인 시각 컬럼도, Spring Security 이벤트 리스너도 없다.

> **이것이 가장 큰 범위 문제다.** P2 의 대표 규칙인 "로그인 실패 반복" 은 원천 신호가
> **존재하지 않는다**. 아래 2절의 결정이 선행되어야 P2 가 시작할 수 있다.

### 1.3 전송 수단

- **Outbox 없음.** 전송 대기 기록을 남기는 구조가 없다.
- 밖으로 나가는 HTTP 는 `TossPaymentClient` 와 `SolapiKakaoAlimtalkClient` 둘뿐이다.
- `WebhookEvent` 엔티티는 **빈 껍데기**다 — `// TODO: 필드 정의 (ERD 확정 후)`.
  외부 이벤트를 저장하는 패턴이 이미 있다고 오해하면 안 된다.
- 결제사(Toss) 연동은 전부 CakeShop 안에서 끝난다. **플랫폼은 Toss 를 호출하지 않는다.**

---

## 2. 결정해야 할 것

### D-1. 로그인 신호 확보 — **A 안 채택** (2026-10-16)

검토한 안은 셋이었다.

| 안 | 내용 | 결과 |
|---|---|---|
| **A** | CakeShop 에 로그인 성공·실패 기록을 추가하고 발행 | **채택** |
| B | 규칙을 "로그인 실패 반복" 대신 거래 중심으로 바꾼다 | 미채택 |
| C | 시연용 합성 이벤트만 쓴다 | 미채택 |

`SCOPE-02`(서비스 A 의 MVP 1 필수 범위)가 요구하는 "CakeShop 로그인·계정 이벤트 수집"
을 만족하는 길은 A 뿐이다. 아래 3절에 구현 방식을 적는다.

### D-2. 전송 신뢰성 — **Outbox 채택** (2026-10-16)

D-1 과 같은 장치를 쓴다. 아래 3절에 함께 적는다.

> 플랫폼 내부의 Event Publication Registry 는 **CakeShop → P1 구간을 복구하지 못한다.**
> 그 구간은 CakeShop 쪽 책임이다.

### D-3. 공통 필드의 타입 — **확정** (2026-10-16)

[공통 필드](common-fields.md) 로 옮겼다. 일곱 개 계약 전부가 쓰는 규칙이라
C-01 안에 두면 다른 담당자가 찾지 못한다.

요약하면 `eventId`·`tenantId` 는 `String`, `occurredAt` 은 `Instant`(UTC),
금액은 `long`(원 단위 정수)이고 이름은 **`...Amount`** 다. `price` 는 단가를
뜻하므로 쓰지 않는다.

---

## 3. CakeShop 쪽 구현 — A 안 + Outbox

핵심 제약은 하나다. **플랫폼이 죽어도 CakeShop 로그인·결제가 막히면 안 된다**(`T-14`).
그래서 **기록과 전송을 분리**한다.

```
로그인 시도
   ↓
[CakeShop] 보낼 내용을 DB 에 적어만 둔다        ← 빠르고 플랫폼과 무관하다
   ↓
[별도 작업] 나중에 플랫폼으로 전송한다           ← 실패하면 재시도한다
```

### 3.1 보낼 이벤트를 쌓아두는 테이블

```sql
outbound_event
  id               BIGINT AUTO_INCREMENT
  event_id         VARCHAR(36)  -- UUID. 전역 유일. 플랫폼의 중복 판정 기준
                               -- 요청에는 sourceEventId 라는 이름으로 담긴다
  event_type       VARCHAR(50)  -- LOGIN_SUCCEEDED / LOGIN_FAILED / ORDER_CREATED / ...
  payload          JSON
  occurred_at      DATETIME(6)
  status           VARCHAR(20)  -- PENDING / SENT / FAILED
  attempts         INT
  next_attempt_at  DATETIME(6)
  created_at       DATETIME(6)
```

주문·결제·취소도 **같은 테이블**을 쓴다. 그쪽은 이미 업무 트랜잭션이 있으므로
**그 트랜잭션 안에서** 한 행을 넣는다. 거래가 롤백되면 이벤트도 같이 사라진다.

### 3.2 로그인 이벤트 수집

**기존 핸들러를 고치지 않는다.** Spring Security 가 이미 이벤트를 발행하고 있으므로
듣기만 하면 된다. `PortalAuthenticationFailureHandler` 와
`RoleBasedAuthenticationSuccessHandler` 의 리다이렉트 동작에 영향을 주지 않는 것이 목적이다.

```
@EventListener AuthenticationSuccessEvent         -> outbound_event INSERT
@EventListener AbstractAuthenticationFailureEvent -> outbound_event INSERT
```

**리스너 안에서 HTTP 를 호출하지 않는다. INSERT 만 한다.**

로그인 이벤트는 업무 트랜잭션 밖(필터 단계)에서 발생하므로 자체 트랜잭션을 쓴다.
다른 업무와 원자적일 필요가 없어 문제되지 않는다.

> 소셜 로그인(`OAuth2LoginSuccessHandler`)도 같은 이벤트로 잡히는지는 구현하면서 확인한다.

### 3.3 전송 작업

`@Scheduled` 로 `PENDING` 행을 꺼내 플랫폼에 POST 한다. 성공하면 `SENT`,
실패하면 `attempts` 를 올리고 `next_attempt_at` 을 뒤로 민다. **중복 전송은 허용한다** —
P1 이 `event_id` 로 거른다.

### 3.4 로그인 이벤트의 payload

| 필드 | 값 |
|---|---|
| `eventType` | `LOGIN_SUCCEEDED` / `LOGIN_FAILED` |
| `subjectType` | `MEMBER` |
| `subjectExternalId` | 회원 ID. **실패 시 없을 수 있다**(3.5 참고) |
| `ip` | 요청에서 읽는다. 지역 변환은 하지 않는다(3.6 참고) |
| `userAgent` | 요청에서 읽는다 |
| `failureReason` | 실패 시에만 |

### 3.5 결정해야 할 것 — 로그인 실패 시 식별자

없는 계정으로 시도하면 회원 ID 가 없다. 그러면 식별자가 **입력한 이메일**이 된다.

- 그대로 보낼 것인가, 해시로 보낼 것인가?
- 개인정보 범위 문제라 `T-07`(고객사별 접근·증거 격리) 리뷰에서 걸릴 수 있다.

**P1 · P2 가 1주차에 정한다.**

### 3.6 IP 와 지역

IP 는 CakeShop 이 요청에서 읽어 payload 에 담는 **데이터**다. 실제 네트워크 경로와 무관하다.
시연에서 다른 지역 접속을 보이려면 VPN 이 아니라 **이벤트의 ip 값을 바꾸면 된다.**

IP 를 지역 이름으로 바꾸려면 GeoIP 데이터베이스가 필요하다. 4주 범위에서는
**IP 만 수집하고 규칙도 IP 수준으로 만든다** — "직전 로그인과 다른 IP 대역" 같은 식이다.
지역 판정은 후속 후보로 둔다.

### 3.7 주의 — 로그인 실패는 폭증할 수 있다

공격을 받으면 분당 수천 건이 쌓인다. 상한을 둘지, 같은 IP·계정의 연속 실패를 묶을지는
1주차에 실제 동작을 보고 정한다. 처음부터 복잡하게 만들지 않는다.

---

## 4. 플랫폼 쪽 수신 — 채택

**`POST /internal/integrations/events`**

### 4.1 Request

| 필드 | 뜻 |
|---|---|
| `sourceEventId` | CakeShop 이 만든 이벤트 고유 ID |
| `eventType` | 로그인 · 주문 · 결제 · 취소 등 종류 |
| `schemaVersion` | 이벤트 형식의 버전 |
| `occurredAt` | **CakeShop 에서 일어난** 시각 |
| `subjectType` / `subjectExternalId` | 조사 대상의 종류와 원천 ID |
| `orderExternalId` / `paymentExternalId` | 있을 때만 — 주문 · 결제 원천 ID |
| `payload` | 허용한 이벤트 상세 값 |

### 4.2 Response

| 필드 | 뜻 |
|---|---|
| `eventId` | **플랫폼이** 저장한 이벤트 ID |
| `ingestStatus` | 저장 완료 / 기존 이벤트 중복 접수 |
| `receivedAt` | 플랫폼 수신 시각 |

> `sourceEventId`(CakeShop 것)와 `eventId`(플랫폼 것)는 **다른 값**이다.
> 자세한 구분은 [공통 필드](common-fields.md) 참고.

### 4.3 처리 규칙

- 고객사와 연동 ID 는 **인증된 CakeShop 연동 정보로 결정한다.**
  요청 본문의 `tenantId` 를 그대로 믿지 않는다.
- 새 이벤트를 **영속 저장한 뒤** 접수 응답(202)을 반환한다. 같은 원천 ID 로 같은
  내용이 다시 오면 기존 결과(200)를 반환할 수 있다.
- **같은 ID 에 다른 내용이면 덮어쓰지 않고 충돌(409)로 처리한다.**
- **이 응답은 P2 탐지나 P5 정산까지 끝났다는 뜻이 아니다.**
- 플랫폼 장애가 CakeShop 의 기존 로그인·주문·결제·취소를 막지 않는다(`T-14`).

### 4.4 아직 확정하지 않은 것

- **인증 헤더** — 서비스 간 인증 방식과 토큰·서명 헤더.
- CakeShop 실제 이벤트 종류 · 원천 ID · 발행 훅은 P1 · P2 · P5 가
  기존 코드와 대조해 확정한다.

---

## 5. 다음 단계

- [x] **D-1**(로그인 신호 확보) — A 안 채택
- [x] **D-2**(전송 신뢰성) — Outbox 채택
- [x] **D-3**(공통 필드 타입) — [공통 필드](common-fields.md) 로 확정
- [ ] 로그인 실패 시 식별자를 그대로 보낼지 해시로 보낼지 — P1 · P2 (3.5)
- [ ] CakeShop 에 `outbound_event` 테이블과 리스너·전송 작업 구현 — P1
- [x] HTTP 경로 · 요청/응답 필드를 이 문서에 추가 (4절)
- [ ] 서비스 간 **인증 헤더** 확정 — P1

조사 근거는 CakeShop `dev` 브랜치 기준이다. 재확인은
[team-sweethan/cakeshop](https://github.com/team-sweethan/cakeshop) 에서 한다.
