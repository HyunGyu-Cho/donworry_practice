package com.example.platform;

import java.util.Set;

/**
 * 로그인한 사용자와 그 사용자가 접근할 수 있는 고객사를 담는다.
 *
 * <p>이 타입은 루트 패키지에 직접 둔다. {@code com.example.platform.security} 처럼
 * 하위 패키지를 만들면 Spring Modulith 가 그것을 7번째 업무 모듈로 인식하고,
 * 6개 모듈이 전부 그 모듈을 의존하게 되어 경계 검사가 의미를 잃는다.
 *
 * <p>권한 검증을 필터에서 처리하고 그 결과를 이 타입으로 넘기면,
 * 업무 모듈이 identity 모듈을 컴파일 의존하지 않아도 된다.
 */
public record AuthContext(String tenantId, String userId, Set<String> roles) {

    /** 요청 본문의 tenantId 를 그대로 믿지 않는다. 로그인 정보와 일치하는지 확인한다. */
    public void requireTenant(String target) {
        if (tenantId == null || !tenantId.equals(target)) {
            throw new TenantAccessDeniedException(target);
        }
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public static final class TenantAccessDeniedException extends RuntimeException {
        public TenantAccessDeniedException(String target) {
            super("요청한 고객사에 접근할 수 없습니다: " + target);
        }
    }
}
