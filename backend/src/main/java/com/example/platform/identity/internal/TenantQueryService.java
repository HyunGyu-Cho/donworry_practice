package com.example.platform.identity.internal;

import com.example.platform.identity.TenantQueryApi;
import org.springframework.stereotype.Service;

/**
 * 공개 인터페이스는 모듈 루트에, 구현은 {@code internal} 에 둔다.
 * 다른 모듈은 인터페이스만 보고 구현은 보지 못한다.
 */
@Service
class TenantQueryService implements TenantQueryApi {

    @Override
    public boolean isActive(String tenantId) {
        // P1-IDENTITY-01 에서 구현한다.
        return true;
    }
}
