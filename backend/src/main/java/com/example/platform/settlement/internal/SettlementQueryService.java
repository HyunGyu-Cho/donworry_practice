package com.example.platform.settlement.internal;

import com.example.platform.settlement.SettlementQueryApi;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * C-07 조회의 구현.
 *
 * <p>조회 실패를 예외로 던지지 않고 resultStatus 로 돌려준다. 예외로 던지면
 * cashflow 가 "자료 없음"과 구분할 수 없고 T-12 가 깨진다.
 */
@Service
class SettlementQueryService implements SettlementQueryApi {

    @Override
    public ExpectedSettlementView findExpected(
            String tenantId, LocalDate fromDate, LocalDate toDate) {
        // P5-C07-01 에서 구현한다.
        return ExpectedSettlementView.empty(ExpectedSettlementView.ResultStatus.NO_DATA);
    }
}
