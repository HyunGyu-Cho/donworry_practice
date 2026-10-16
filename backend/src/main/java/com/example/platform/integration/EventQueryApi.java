package com.example.platform.integration;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AI 조사 증거로 쓸 원천 이벤트를 돌려주는 공개 창구.
 *
 * <p>마스킹은 데이터를 소유한 이 모듈이 한다. 어떤 필드가 민감한지 아는 쪽이
 * 소유자이기 때문이다. 호출하는 쪽(investigation)은 범위만 통제한다.
 */
public interface EventQueryApi {

    /**
     * 지정한 고객사의, 지정한 ID 집합에 속한 이벤트만 돌려준다.
     * 범위 밖 ID 는 조용히 제외한다.
     */
    List<SourceEventEvidenceView> findForEvidence(String tenantId, Collection<String> eventIds);

    /** 증거로 내보내도 되는 형태. 원본 개인정보는 들어가지 않는다. */
    record SourceEventEvidenceView(
            String eventId,
            String tenantId,
            String eventType,
            Instant occurredAt,
            Map<String, String> maskedAttributes) {}
}
