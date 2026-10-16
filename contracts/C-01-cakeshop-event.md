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

### D-1. 로그인 신호를 어떻게 확보할 것인가 — P1 · P2, 1주차 안

| 안 | 내용 | 비용 | 위험 |
|---|---|---|---|
| **A** | CakeShop 에 로그인 성공·실패 기록을 추가하고 발행 | CakeShop 수정 | 기존 앱을 건드린다 |
| **B** | 규칙을 "로그인 실패 반복" 대신 거래 중심으로 바꾼다 | 기획 변경 | 서비스 A 의 핵심 시나리오가 약해진다 |
| **C** | 시연용 합성 이벤트만 쓴다 | 가장 쌈 | 실제 연동을 증명하지 못한다 |

**A 를 권한다.** `PortalAuthenticationFailureHandler` 와
`RoleBasedAuthenticationSuccessHandler` 에 기록을 더하는 것은 작은 변경이고,
`SCOPE-02` 가 요구하는 "CakeShop 로그인·계정 이벤트 수집" 을 만족하는 유일한 길이다.
다만 `T-14`(플랫폼 장애가 CakeShop 로그인을 막지 않음)를 깨지 않도록
**기록과 전송을 분리**해야 한다.

### D-2. 전송 신뢰성 — P1, 1주차 안

플랫폼이 잠시 죽어도 CakeShop 의 로그인·결제가 막히면 안 된다(`T-14`).

**Outbox 를 권한다.** 업무 트랜잭션 안에서 전송 대기 행만 남기고, 별도 작업이 HTTP 로
보낸다. 실패하면 백오프 재시도하고 중복 전송은 허용한다. P1 이 `sourceEventId` 와
내용 해시로 중복·충돌을 거른다.

> 플랫폼 내부의 Event Publication Registry 는 **CakeShop → P1 구간을 복구하지 못한다.**
> 그 구간은 CakeShop 쪽 책임이다.

### D-3. 공통 필드의 타입 — 전원, 0-A 연장

| 필드 | 후보 | 비고 |
|---|---|---|
| `eventId` | `String` (UUID 문자열) | CakeShop 이 생성. 전역 유일 |
| `tenantId` | `String` | 연동 인증에서 도출한다. **요청 본문의 값을 믿지 않는다** |
| `occurredAt` | `Instant` (UTC) | 화면에서만 KST 로 바꾼다 |
| 금액 | `long` | 원 단위 정수 |
| `schemaVersion` | `int` | |

---

## 3. 수신 규칙 — 채택

- P1 이 이벤트 ID · 버전 · 고객사 연결 정보를 검증한 뒤 저장한다.
- **같은 원천 이벤트가 다시 와도 한 번만 반영한다.**
- **같은 ID 에 다른 내용이면 충돌로 거절하고 기존 데이터를 덮어쓰지 않는다** (HTTP 409).
- `tenantId` 는 인증된 연동 정보에서 도출한다. 요청 본문의 값을 그대로 믿지 않는다.
- 플랫폼 장애가 CakeShop 의 기존 로그인·주문·결제·취소를 막지 않는다.

---

## 4. 다음 단계

- [ ] D-1 결정 — P1 · P2
- [ ] D-2 결정 — P1
- [ ] D-3 결정 — 전원
- [ ] 결정 뒤 이 문서의 1절을 "확정된 이벤트 목록" 으로 다시 쓴다
- [ ] HTTP 경로 · 요청/응답 필드 · 인증 헤더를 이 문서에 추가한다

조사 근거는 CakeShop `dev` 브랜치 기준이다. 재확인은
[team-sweethan/cakeshop](https://github.com/team-sweethan/cakeshop) 에서 한다.
