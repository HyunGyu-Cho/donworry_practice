package com.example.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 실행 애플리케이션은 하나다. 업무 모듈 6개는 이 패키지의 직속 하위 패키지이며,
 * Spring Modulith 가 그 경계를 검사한다.
 */
@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
