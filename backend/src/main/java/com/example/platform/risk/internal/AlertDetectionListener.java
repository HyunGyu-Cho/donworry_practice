package com.example.platform.risk.internal;

import com.example.platform.integration.events.SourceEventReceived;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * integration 이 발행한 원천 이벤트를 받아 위험 규칙을 평가한다.
 *
 * <p>여기서 integration 을 import 하므로 컴파일 의존은 risk → integration 이다.
 * 데이터는 integration → risk 로 흐르는데 의존은 반대 방향이라는 점에 주의한다.
 *
 * <p>{@code @ApplicationModuleListener} 는 커밋 이후 비동기로 실행되며,
 * 처리 기록이 Event Publication Registry 에 남는다. settlement 쪽 리스너와 완전히
 * 독립이라, 한쪽이 실패해도 다른 쪽의 성공은 유지된다(T-02).
 */
@Component
class AlertDetectionListener {

    @ApplicationModuleListener
    void on(SourceEventReceived event) {
        // P2-RISK-01 에서 구현한다. 지금은 경계와 배선만 확인한다.
    }
}
