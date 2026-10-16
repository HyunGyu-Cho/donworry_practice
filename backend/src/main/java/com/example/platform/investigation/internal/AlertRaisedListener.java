package com.example.platform.investigation.internal;

import com.example.platform.risk.events.AlertRaised;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * risk 가 저장한 경보를 받아 Case 를 만들지 판단한다.
 *
 * <p>채택된 정책 — HIGH 1건이면 Case 생성 + 최초 AI Job 자동 저장.
 * 같은 고객사 · 같은 대상에서 서로 다른 규칙의 MEDIUM 이상 2건이 30분 이내면 Case 만 생성.
 * LOW 1건 · MEDIUM 단독 · 같은 규칙 중복은 Case 를 만들지 않는다(정상이다).
 *
 * <p>Case 생성 · Alert 연결 · 최초 AI Job 저장은 한 트랜잭션으로 커밋하고,
 * FastAPI 원격 호출은 그 뒤에 따로 한다.
 */
@Component
class AlertRaisedListener {

    @ApplicationModuleListener
    void on(AlertRaised event) {
        // P3-CASE-01 에서 구현한다. 지금은 경계와 배선만 확인한다.
    }
}
