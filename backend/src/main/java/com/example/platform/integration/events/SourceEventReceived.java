package com.example.platform.integration.events;

import java.time.Instant;

/**
 * C-02 — CakeShop 원천 이벤트를 저장하고 커밋한 뒤 발행한다.
 * risk 와 settlement 가 각각 독립적으로 받는다.
 *
 * <p>한쪽이 실패해도 다른 쪽의 성공은 유지된다. 다만 Registry 가 금액 중복까지
 * 막아 주지는 않으므로, 받는 쪽이 eventId 에 DB 고유 제약을 걸어야 한다.
 */
public record SourceEventReceived(
        String eventId,
        String tenantId,
        int schemaVersion,
        String eventType,
        Instant occurredAt,
        String payload) {}
