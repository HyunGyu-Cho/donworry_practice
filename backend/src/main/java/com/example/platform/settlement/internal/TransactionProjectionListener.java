package com.example.platform.settlement.internal;

import com.example.platform.integration.events.SourceEventReceived;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 확정된 결제 · 취소 · 환불 이벤트를 받아 예상 정산에 반영한다.
 *
 * <p>risk 의 리스너와 완전히 독립이다. risk 쪽 처리가 실패해도 여기서 이미 끝난
 * 정산 결과는 되돌아가지 않는다(T-02).
 *
 * <p>같은 이벤트가 다시 와도 금액이 두 번 반영되면 안 된다. Registry 는 전달 기록만
 * 관리하므로, eventId 에 DB 고유 제약을 거는 것은 이 모듈의 책임이다.
 */
@Component
class TransactionProjectionListener {

    @ApplicationModuleListener
    void on(SourceEventReceived event) {
        // P5-PROJECTION-01 에서 구현한다. 지금은 경계와 배선만 확인한다.
    }
}
