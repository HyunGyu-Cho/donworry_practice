package com.example.platform.identity;

/**
 * 고객사가 실재하고 사용 중인지 확인하는 공개 창구.
 *
 * <p>모듈 루트에 있으므로 다른 모듈이 {@code allowedDependencies = {"identity"}} 를
 * 선언하면 쓸 수 있다. 현재는 integration 만 선언하고 있다.
 */
public interface TenantQueryApi {

    /** 해당 고객사가 존재하고 활성 상태이면 true. */
    boolean isActive(String tenantId);
}
