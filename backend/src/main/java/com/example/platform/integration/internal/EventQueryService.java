package com.example.platform.integration.internal;

import com.example.platform.identity.TenantQueryApi;
import com.example.platform.integration.EventQueryApi;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 증거용 원천 이벤트 조회의 구현.
 *
 * <p>여기서 identity 를 쓰므로 integration 의 {@code allowedDependencies = {"identity"}}
 * 선언이 실제로 의미를 갖는다.
 */
@Service
class EventQueryService implements EventQueryApi {

    private final TenantQueryApi tenantQuery;

    EventQueryService(TenantQueryApi tenantQuery) {
        this.tenantQuery = tenantQuery;
    }

    @Override
    public List<SourceEventEvidenceView> findForEvidence(
            String tenantId, Collection<String> eventIds) {
        if (!tenantQuery.isActive(tenantId)) {
            return List.of();
        }
        // P1-INTEGRATION-01 에서 구현한다.
        // 마스킹은 데이터를 소유한 이 모듈이 한다. 호출하는 쪽은 범위만 통제한다.
        return List.of();
    }
}
