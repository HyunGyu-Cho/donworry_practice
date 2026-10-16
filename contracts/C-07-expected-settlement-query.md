# C-07 — 예상 정산 유입 조회

> **C-07 란** 자금 전망(P6)이 예상 정산액(P5)을 가져다 쓰는 약속이다.
> 번호 체계는 [계약 지도](README.md#번호가-무슨-뜻인가) 참고.

- **방향** 업무는 P5 → P6 이지만 **호출은 P6 `cashflow` → P5 `settlement`**
- **방식** 동기 내부 공개 Java API (HTTP 주소가 없다)
- **검증** T-11(현금흐름 독립 실행) · T-12(조회 실패와 이중 계산 방지)
- **상태** **업무 계약 채택** / **Java 시그니처는 (추천안)** — P5 · P6 가 첫 Plan 에서 고정

업무 흐름과 호출 방향이 **반대**다. 그래도 순환이 아니므로 문제없다.
`cashflow` 의 `allowedDependencies` 에 `settlement` 가 들어가는 이유다.

---

## 1. 채택된 것 — 바꾸려면 보고한다

1. **호출 방향** — P6 가 P5 의 공개 Java API 를 호출한다.
2. **`expectedAmount` 의 의미** — 취소·환불·가상 수수료를 **이미 반영한 순액**이다.
3. **`resultStatus` 의 세 상태 구분** — 정상 / 자료 없음 / 조회 실패.
4. **P6 의 재계산 금지** — P6 는 P5 의 내부 테이블을 직접 읽거나 정산액을
   임의로 다시 계산하지 않는다.

근거: T-11 · T-12 가 이 항목들을 검증 대상으로 명시한다.

## 2. 입력과 반환

**입력**

| 필드 | 뜻 |
|---|---|
| `tenantId` | 고객사 |
| `fromDate` / `toDate` | 전망 기간 |

**반환 항목**

| 필드 | 뜻 |
|---|---|
| `settlementId` | 예상 정산 ID |
| `latestRevision` | 최신 계산 버전 |
| `expectedAmount` | **취소·환불·가상 수수료 반영 후** 예상 순정산액(원) |
| `expectedDate` | 예상 지급일 |
| `settlementStatus` | 예상 / 취소 / 미확정 구분 |
| `sourceKind` / `mockInfo` | 실제 테스트 거래 · 가상 정책 · Mock 출처 |
| `resultStatus` | 정상 / 자료 없음 / 조회 실패 구분 |

## 3. `resultStatus` 를 예외가 아니라 반환값에 담는다

**이것이 이 계약에서 가장 중요한 한 줄이다.**

예외로 던지면 P6 는 "자료 없음"과 "조회 실패"를 구분할 수 없고, 둘 다
**'유입 0 원'**으로 처리한다. 그러면 T-12 가 깨진다.

| 상태 | 뜻 | P6 의 처리 |
|---|---|---|
| `OK` | 계산했고 결과가 있다. **0 원도 여기다** | 잔액에 반영 |
| `NO_DATA` | 아직 정산 데이터 자체가 없다 | 잔액에 반영하지 **않는다** |
| `QUERY_FAILED` | 조회가 실패했다 | 화면에 **오류로 표시** |

## 4. 현재 저장소의 시그니처

`backend/src/main/java/com/example/platform/settlement/SettlementQueryApi.java` 에
들어 있다. 이름과 DTO 모양은 **(추천안)** 이다.

```java
public interface SettlementQueryApi {
    ExpectedSettlementView findExpected(String tenantId, LocalDate fromDate, LocalDate toDate);
}
```

> **확인 필요 — P5 · P6**
> 설계서 03-3 의 초안과 저장소 코드의 이름·타입이 다르다. 업무 계약은 같고
> 시그니처만 다르므로, 첫 Plan 에서 한쪽으로 고정하면 된다.
>
> | | 설계서 03-3 초안 | 현재 저장소 코드 |
> |---|---|---|
> | 인터페이스 | `SettlementForecastQuery` | `SettlementQueryApi` |
> | 메서드 | `findExpectedInflow` | `findExpected` |
> | 반환 | `ExpectedSettlementResult` · `ExpectedSettlementItem` | `ExpectedSettlementView` · `Item` |
> | `settlementId` | `Long` | `String` |
> | `latestRevision` | `int` | `long` |
> | `settlementStatus` | `SettlementStatus` enum | `String` |
>
> 반환이 **단건인지 기간 내 목록인지**도 함께 고정한다(현재 코드는 목록이다).
> 타입은 [공통 필드](common-fields.md) 를 따른다 — enum 은 이름 문자열로 주고받는다.
