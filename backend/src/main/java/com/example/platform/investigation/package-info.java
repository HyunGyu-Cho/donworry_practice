/**
 * P3 — 사건(Case) 생성 · 연결, 직원 판정, 조치 권고, AI 조사 Job.
 *
 * <p>공개 API 가 없다. 아무도 이 모듈을 의존하지 않는 최하류이기 때문이다.
 * 대신 증거 수집 때문에 셋을 의존한다 — risk 의 이벤트(구독), risk 의 조회 API,
 * integration 의 조회 API.
 *
 * <p>허브처럼 보이지만 <b>아래쪽 허브</b>라 괜찮다. 이 모듈이 무엇을 더 의존하든
 * 위쪽 경계는 무너지지 않는다.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = { "risk::events", "risk", "integration" })
package com.example.platform.investigation;
