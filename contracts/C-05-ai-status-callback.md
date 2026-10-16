# C-05 — AI 실행 상태 통지

> **C-05 란** AI 서버가 조사 진행 상태를 플랫폼에 알려 주는 약속이다.
> 번호 체계는 [계약 지도](README.md#번호가-무슨-뜻인가) 참고.

- **방향** P4 `ai-server`(FastAPI) → Spring Boot P3 `investigation`
- **방식** 내부 HTTP — **`POST /internal/ai/jobs/{jobId}/status`**
- **검증** T-06(AI 중복·장애 복구)
- **상태** **기본 원칙 채택**

---

## 1. Request

| 필드 | 뜻 |
|---|---|
| `jobId` | 원래 요청 ID |
| `runId` | 실행 추적 ID |
| `stateSequence` | **순서가 증가하는** 상태 번호 |
| `status` | `ACCEPTED` / `RUNNING` / `SUCCEEDED` / `FAILED` |
| `reportId` / `reportVersion` | 보고서가 끝났을 때의 참조 ID 와 버전 |
| `errorCode` | 실패했을 때, **외부에 보여도 되는** 오류 코드 |

## 2. Response

| 필드 | 뜻 |
|---|---|
| `jobId` | 확인한 작업 ID |
| `applied` | 이번 상태가 반영되었는지 |
| `currentStatus` | 현재 저장된 작업 상태 |

## 3. 처리 규칙

- **`SUCCEEDED` 는 보고서가 정상 저장·검증된 뒤에만 통지한다.**
- 이미 처리한 상태 번호나 **늦게 도착한 과거 상태는 무시한다.**
  `stateSequence` 가 있는 이유다.
- 상태 통지가 실패해도 **저장된 실행 상태를 조회해 복구**할 수 있어야 한다.
- **FastAPI 가 사건 판정·조치 완료를 직접 바꾸지 않는다.** 판정은 사람이 한다.

> `applied: false` 가 오류는 아니다. "그 상태는 이미 지난 것이라 반영하지 않았다"는
> 정상 응답이다.
