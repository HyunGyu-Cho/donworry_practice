/**
 * integration 이 발행하는 공개 이벤트.
 *
 * <p>{@code @NamedInterface} 덕분에 다른 모듈이 {@code "integration::events"} 로
 * <b>이벤트만</b> 의존할 수 있다. 조회 API 까지 열어 줄 필요가 없다.
 */
@org.springframework.modulith.NamedInterface("events")
package com.example.platform.integration.events;
