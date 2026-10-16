/**
 * P2 — 로그인 · 계정 · 거래 위험 규칙과 경보(Alert).
 *
 * <p>의존은 integration 의 <b>이벤트만</b>이다. settlement 와 cashflow 는 선언에
 * 없으므로, 자금 쪽 타입을 import 하는 순간 빌드가 실패한다. 보안 흐름과 자금 흐름이
 * 독립이라는 결정을 코드로 강제하는 자리다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "integration::events" })
package com.example.platform.risk;
