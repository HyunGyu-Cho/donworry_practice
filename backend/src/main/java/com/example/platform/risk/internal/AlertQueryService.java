package com.example.platform.risk.internal;

import com.example.platform.risk.AlertQueryApi;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/** 증거용 경보 조회의 구현. 탐지 근거 요약은 이 모듈이 만들고 원문 개인정보는 넣지 않는다. */
@Service
class AlertQueryService implements AlertQueryApi {

    @Override
    public List<AlertEvidenceView> findForEvidence(String tenantId, Collection<String> alertIds) {
        // P2-ALERT-01 에서 구현한다. 범위 밖 alertId 는 조용히 제외한다.
        return List.of();
    }
}
