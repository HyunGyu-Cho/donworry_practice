package com.example.platform.risk;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * AI 조사 증거로 쓸 경보를 돌려주는 공개 창구.
 *
 * <p>탐지 근거 요약은 이 모듈이 만든다. 원문 개인정보는 넣지 않는다.
 */
public interface AlertQueryApi {

    /**
     * 지정한 고객사의, 지정한 ID 집합에 속한 경보만 돌려준다.
     * 범위 밖 ID 는 조용히 제외한다.
     */
    List<AlertEvidenceView> findForEvidence(String tenantId, Collection<String> alertIds);

    record AlertEvidenceView(
            String alertId,
            String tenantId,
            RiskLevel riskLevel,
            String ruleCode,
            String ruleRevisionId,
            Instant occurredAt,
            List<String> evidenceEventIds,
            String detectionSummary) {}
}
