package com.readour.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(405, "허용되지 않은 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(409, "요청이 충돌했습니다.", HttpStatus.CONFLICT),
    UNPROCESSABLE_ENTITY(422, "요청을 처리할 수 없습니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_GATEWAY(502, "게이트웨이 오류입니다.", HttpStatus.BAD_GATEWAY),
    SERVICE_UNAVAILABLE(503, "서비스를 사용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    GATEWAY_TIMEOUT(504, "게이트웨이 타임아웃입니다.", HttpStatus.GATEWAY_TIMEOUT);

    private final int status;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(int status, String defaultMessage, HttpStatus httpStatus) {
        this.status = status;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
