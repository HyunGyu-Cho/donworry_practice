/**
 * P1 — 고객사(Tenant) · 플랫폼 사용자 · 역할 · 접근 권한.
 *
 * <p>의존하는 모듈이 없다. 권한 검증은 필터에서 하고 그 결과를
 * {@link com.example.platform.AuthContext} 로 넘기므로, 다른 업무 모듈이
 * 이 모듈을 컴파일 의존하지 않는다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.example.platform.identity;
