/**
 * P5 — 테스트 거래 반영, 가상 예상 정산, Mock 입금과 기초 대사.
 *
 * <p>risk 와 똑같이 integration 의 이벤트 하나만 의존한다. 보안 쪽 타입은 선언에 없다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "integration::events" })
package com.example.platform.settlement;
