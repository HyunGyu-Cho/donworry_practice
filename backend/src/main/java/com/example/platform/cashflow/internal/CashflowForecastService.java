package com.example.platform.cashflow.internal;

import com.example.platform.settlement.SettlementQueryApi;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 날짜별 예상 잔액 = 전일 예상 잔액 + 해당일 예상 유입 − 해당일 예정 지출.
 *
 * <p>P5 가 준 expectedAmount 는 이미 취소 · 환불 · 수수료를 반영한 순액이다.
 * 여기서 다시 빼면 이중 차감이고 T-12 가 깨진다. Mock 입금도 중복 합산하지 않는다.
 */
@Service
@Transactional(readOnly = true)
class CashflowForecastService {

    private final SettlementQueryApi settlementQuery;

    CashflowForecastService(SettlementQueryApi settlementQuery) {
        this.settlementQuery = settlementQuery;
    }

    /** P6-FORECAST-01 에서 구현한다. */
    void forecast(String tenantId, LocalDate from, LocalDate to) {
        // 정상 0원 · 자료 없음 · 조회 실패를 각각 다르게 다뤄야 한다.
        SettlementQueryApi.ExpectedSettlementView view =
                settlementQuery.findExpected(tenantId, from, to);
    }
}
