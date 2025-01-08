package org.example.playus.global.exception;

public enum TokenError {
    INVALID_SIGN("유효하지 않는 JWT"),
    EXPRIED("만료"),
    UNSUPPORTED("지원하지 않는 JWT"),
    EMPTY_CLAIMS("잘못된 JWT"),
    VALID("유효한 토큰");

    private final String error;

    TokenError(String error) {
        this.error = error;
    }
}
