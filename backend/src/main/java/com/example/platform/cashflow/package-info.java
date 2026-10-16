/**
 * P6 — 시작 자금, 예정 지출, 날짜별 예상 잔액과 부족 경고.
 *
 * <p>의존은 settlement 하나뿐이고, 그마저도 선택이다. 정산 데이터가 전혀 없어도
 * 시작 자금과 예정 지출만으로 전망이 계산되어야 한다(T-11).
 * 보안 쪽 타입은 선언에 없다.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "settlement" })
package com.example.platform.cashflow;
