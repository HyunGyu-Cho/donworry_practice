# C-04A — AI 용 증거 조회

> **C-04A 란** AI 서버가 조사에 쓸 증거를 플랫폼에 되묻는 약속이다.
> C-04(조사 작업 접수)의 짝이라 `A` 를 붙였다.
> 번호 체계는 [계약 지도](README.md#번호가-무슨-뜻인가) 참고.

- **방향** P4 `ai-server`(FastAPI) → Spring Boot P3 `investigation`
- **방식** 내부 HTTP — **`GET /internal/ai/jobs/{jobId}/evidence`**
- **검증** T-07(고객사별 접근·증거 격리)
- **상태** **기본 원칙 채택**

---

## 1. Request

| 필드 | 뜻 |
|---|---|
| `jobId` | 발급된 조사 작업 ID (경로 변수) |
| `evidenceType` | 필요한 증거 종류 (선택) |

## 2. Response

| 필드 | 뜻 |
|---|---|
| `jobId` | 조회 기준 작업 ID |
| `evidence` | 허용된 이벤트·Alert ID, 발생 시각, 출처, **마스킹된** 증거 스냅샷 목록 |
| `evidenceBasisVersion` | 증거 조회 기준 버전 |

## 3. 처리 규칙

- FastAPI 는 **CakeShop DB 나 다른 업무 모듈의 Repository 에 직접 접근하지 않는다.**
- 허용된 작업·고객사·Case 범위만 조회한다.
  **요청자가 보낸 `tenantId` 만으로 접근을 허용하지 않는다.**
- **증거가 없는 정상 응답(빈 목록)과 조회 실패를 구분한다.**

## 4. 권한 검사는 한 곳에서만 한다

`tenantId` 를 요청에서 받지 않는 것이 이 계약의 핵심이다. 받으면 AI 서버가
거짓말할 여지가 생긴다. 대신 **`jobId` 하나에서 거꾸로 알아낸다.**

```
jobId → 저장된 Job → caseId → 그 Case 의 tenantId → 이 범위만 조회
```

구현은 `investigation` 모듈의 증거 게이트 한 곳에 모은다
(`investigation/internal/EvidenceGatewayService.java`).
**마스킹은 데이터를 가진 모듈이 한다.** 게이트가 남의 모듈 데이터를 직접 가공하지 않는다.
