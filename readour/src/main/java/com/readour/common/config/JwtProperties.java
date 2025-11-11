package com.readour.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64 인코딩된 HMAC 서명 키
     */
    private String secret;

    /**
     * 토큰 발급자 정보
     */
    private String issuer;

    /**
     * 액세스 토큰 만료 시간 (ms)
     */
    private long accessTokenExpirationMillis;

}
