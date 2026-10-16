package com.example.platform.risk.events;

import com.example.platform.risk.RiskLevel;
import java.time.Instant;
import java.util.List;

/**
 * C-03 — Alert 를 DB 에 저장한 뒤 발행한다. Case 생성 여부는 investigation 이 판단한다.
 *
 * <p>caseId · linked · autoAiRequested 를 즉시 돌려주지 않는다. 내부 이벤트이므로
 * HTTP 202 같은 응답 코드도 쓰지 않는다. 결과는 기존 Alert · Case 조회 경로에서 확인한다.
 *
 * <p>occurredAt 은 도착 시각이나 DB 저장 시각이 아니라 <b>실제 발생 시각</b>이다.
 * 30분 연관 판단이 도착 순서 때문에 왜곡되면 안 되기 때문이다.
 */
public record AlertRaised(
        String alertId,
        String tenantId,
        String subjectType,
        String subjectExternalId,
        RiskLevel riskLevel,
        String ruleCode,
        String ruleRevisionId,
        Instant occurredAt,
        List<String> evidenceEventIds) {}
