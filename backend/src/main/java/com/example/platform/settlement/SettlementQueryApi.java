package com.example.platform.settlement;

import java.time.LocalDate;
import java.util.List;

/**
 * C-07 — cashflow 가 예상 정산 유입을 조회하는 공개 창구.
 *
 * <p>업무 흐름은 settlement → cashflow 지만 호출은 cashflow → settlement 다.
 * 방향이 반대여도 순환이 아니므로 문제없다.
 *
 * <p>업무 계약(방향 · 필드 의미 · 재계산 금지)은 채택된 사항이고,
 * 아래 메서드 이름과 DTO 모양은 구현 추천안이다. P5 · P6 가 첫 Plan 에서 고정한다.
 */
public interface SettlementQueryApi {

    ExpectedSettlementView findExpected(String tenantId, LocalDate fromDate, LocalDate toDate);

    /**
     * 조회 실패를 예외가 아니라 반환값에 담는 것이 핵심이다.
     * 예외로 던지면 cashflow 가 "자료 없음"과 구분할 수 없고 T-12 가 깨진다.
     */
    record ExpectedSettlementView(ResultStatus resultStatus, List<Item> items) {

        public enum ResultStatus {
            /** 계산했고 결과가 있다. 0원도 여기에 해당한다. */
            OK,
            /** 아직 정산 데이터 자체가 없다. 잔액에 반영하지 않는다. */
            NO_DATA,
            /** 조회가 실패했다. 화면에 오류로 표시한다. */
            QUERY_FAILED
        }

        /** OK 가 아니면 items 는 빈 목록이다. */
        public static ExpectedSettlementView empty(ResultStatus status) {
            return new ExpectedSettlementView(status, List.of());
        }
    }

    record Item(
            String settlementId,
            long latestRevision,
            /** 취소 · 환불 · 가상 수수료를 이미 반영한 순정산액(원 단위 정수). 다시 빼면 이중 차감이다. */
            long expectedAmount,
            LocalDate expectedDate,
            String settlementStatus,
            String sourceKind,
            String mockInfo) {}
}
