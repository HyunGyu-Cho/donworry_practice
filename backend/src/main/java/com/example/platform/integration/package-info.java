/**
 * P1 — CakeShop 연동 설정과 원천 이벤트 수신 · 검증 · 저장 · 분배.
 *
 * <p>업무 흐름의 최상류다. risk 와 settlement 는 이 모듈이 발행한 이벤트를 받지만,
 * 이 모듈은 둘의 존재를 모른다. 이벤트는 <b>받는 쪽이 보내는 쪽을 아는</b> 구조이기 때문이다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "identity" })
package com.example.platform.integration;
