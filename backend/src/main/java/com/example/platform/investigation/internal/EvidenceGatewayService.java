package com.example.platform.investigation.internal;

import com.example.platform.integration.EventQueryApi;
import com.example.platform.risk.AlertQueryApi;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * C-04A — AI 서버가 증거를 요청할 때 통과하는 <b>단 하나의 권한 게이트</b>.
 *
 * <p>모듈마다 증거 API 를 열면 권한 검증이 세 곳으로 흩어지고, risk 는 Case 를 모르므로
 * "이 경보가 그 사건에 속하는가"를 판단할 수조차 없다. 그래서 Case 를 소유한
 * 이 모듈이 범위를 정하고, 각 소유 모듈은 자기 데이터만 마스킹해서 내놓는다.
 *
 * <p>AI 가 보낸 tenantId · caseId 는 쓰지 않는다. jobId 하나에서 전부 도출한다.
 * 그래서 이 서비스의 입력에 tenantId 가 없다.
 */
@Service
@Transactional(readOnly = true)
class EvidenceGatewayService {

    private final AlertQueryApi alertQuery;
    private final EventQueryApi eventQuery;

    EvidenceGatewayService(AlertQueryApi alertQuery, EventQueryApi eventQuery) {
        this.alertQuery = alertQuery;
        this.eventQuery = eventQuery;
    }

    /**
     * jobId 로 범위를 도출한 뒤 그 안에서만 증거를 모은다.
     * P3-AIJOB-01 에서 구현한다.
     */
    List<AlertQueryApi.AlertEvidenceView> collect(String jobId) {
        // 1. jobId -> AI Job 조회 -> tenantId · caseId 도출
        // 2. caseId -> 연결된 alertId 집합 = 허용 범위
        // 3. 그 범위로 alertQuery · eventQuery 호출
        return List.of();
    }
}
