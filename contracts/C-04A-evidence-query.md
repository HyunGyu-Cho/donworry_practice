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

> 실제 제품도 같은 방식이다. Microsoft 의 보안 조사 에이전트는 권한을 다섯 개로 쪼개고,
> 메일 권한은 **"경보에 연결된 메일만"** 으로 제한한다. 문서가 이유를 직접 적었다 —
> *"this more limited permission provides the agent with access only to emails
> associated with alerts, rather than all emails."* 전체 읽기를 주지 않는다.

---

## 5. AI 가 쓰는 도구 — 넷이면 된다

조사 도구를 많이 줄 필요가 없다. 참고한 제품의 에이전트는 도구가 **다섯 개**였고
그중 **실제로 자료를 캐는 것은 하나**였다. 나머지는 케이스 생성·채널 생성·현재 시각이었다.

| 도구 | 돌려주는 것 |
|---|---|
| `get_case_context(jobId)` | 사건·경보, 근거 이벤트 ID, 발생 시각, 출처 |
| `get_evidence(jobId, evidenceType)` | 마스킹된 증거 스냅샷 목록 (3절의 게이트를 지난다) |
| `get_subject_history(jobId, window)` | 같은 조사 대상의 최근 이벤트 |
| `report_status(jobId, ...)` | 실행 상태 통지 — [C-05](C-05-ai-status-callback.md) 로 나간다 |

**어느 도구도 `tenantId` 를 받지 않는다.** 전부 `jobId` 에서 도출한다(4절).

## 6. 조회 결과는 세 가지로 답한다 — 가장 중요한 규칙

경보 1,247건으로 LLM 조사를 평가한 연구에서, **모든 방식이 공격 관련 경보를
최소 40.4% 놓쳤다.** 1순위 실패 원인이 이것이다.

> *"Attack alerts are more likely to be dismissed when searches return no records."*
> 검색 결과가 비면 공격 경보를 기각할 확률이 올라간다.

**"없다"와 "못 봤다"를 구분하지 못해서 생기는 일이다.** 그래서 모든 조회 도구는
세 가지를 구분해 돌려준다.

| 결과 | 뜻 | AI 가 해야 하는 것 |
|---|---|---|
| `OK` | 찾았다 | 조사를 계속한다 |
| `NO_DATA` | 조회는 됐고 결과가 없다 | **"없다"고만 기록한다. "이상 없음"으로 결론 내리지 않는다** |
| `QUERY_FAILED` | 조회 자체가 실패했다 | **사람에게 올린다.** 보고서를 쓰지 않는다 |

[C-07](C-07-expected-settlement-query.md)(예상 정산 유입 조회)에서 `resultStatus` 를
예외가 아니라 반환값에 담은 것과 **같은 장치**다. 이유도 같다 — 예외로 던지면
받는 쪽이 "자료 없음"과 "조회 실패"를 구분할 수 없다.

> 같은 연구에서 **같은 맥락 안에서의 자기 검토(self-review)는 순효과가 마이너스**였다.
> AI 가 자기 결론을 자기가 다시 검토하게 만드는 것은 도움이 되지 않는다.
> 가장 성적이 좋은 방식도 **18.4% 를 사람에게 올렸다.**
>
> 우리 설계의 **"최종 판정은 사람이 한다"** 가 이 근거와 맞는다.

## 7. 함께 둘 장치

참고한 제품들이 공통으로 갖춘 것 중, 4주 범위에서 쓸 만한 것.

| 장치 | 내용 |
|---|---|
| **전용 신원** | AI 서버에 사람 계정을 재사용하지 않는다. 전용 자격으로 최소 권한만 준다 |
| **판단 과정 기록** | 어떤 도구를 어떤 순서로 불렀는지 남긴다. 직원이 보고서를 검토할 때 필요하다 |
| **보낼 필드 허용 목록** | 모델에 넘길 필드를 화이트리스트로 관리한다. `T-07`(증거 격리) 검증 대상이다 |

세 번째는 Elastic 의 조사 기능이 쓰는 방식이다. 그쪽은 LLM 에 **도구를 주지 않고**
미리 검색한 맥락만 넘기며, 넘길 비표준 필드를 관리자가 허용 목록으로 고른다.

---

## 8. 근거

| 레퍼런스 | 가져온 것 |
|---|---|
| [Microsoft — Phishing/Alert Triage Agent](https://learn.microsoft.com/en-us/defender-xdr/phishing-triage-agent) | "경보에 연결된 것만" 권한, 전용 신원, 판단 과정 기록 |
| [Elastic Security Labs — agentic SOC 평가](https://www.elastic.co/security-labs/blog/llm-benchmarking-agentic-soc) | 도구 다섯 개 중 조사 도구는 하나 |
| [Elastic — Attack Discovery](https://www.elastic.co/docs/solutions/security/ai/attack-discovery) | 도구 없이 맥락만 주는 방식, 필드 허용 목록 |
| [ALERT-BENCH (arXiv 2610.10608)](https://arxiv.org/abs/2610.10608v1) | 40.4% 누락, 빈 결과를 기각으로 오해, 자기 검토의 역효과, 18.4% 사람 상신 |

규칙 쪽 조사는 [탐지 규칙 설계 방침](detection-rules.md) 에 있다.
