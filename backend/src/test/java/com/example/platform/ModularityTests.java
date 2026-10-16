package com.example.platform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * 모듈 경계가 실제로 지켜지는지 검사한다. DB 도 Spring 컨텍스트도 필요 없다.
 *
 * <p>확인하고 싶으면 risk 안에서 settlement 타입을 하나 import 해 보라.
 * 이 테스트가 빨개져야 정상이다. 통과한다면 allowedDependencies 가 안 걸린 것이다.
 */
class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(PlatformApplication.class);

    @Test
    @DisplayName("모듈 간 순환·internal 침범·선언하지 않은 의존이 없다")
    void verifiesModuleStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("탐지된 모듈 구조를 출력한다")
    void printsModuleStructure() {
        modules.forEach(System.out::println);
    }
}
