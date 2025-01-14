package org.example.playus.domain.employee.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenStore {
    private String accessToken;
    private String refreshToken;

    @Builder
    public TokenStore(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public boolean isEmpty() {
        return (this.accessToken == null || this.accessToken.isBlank()) &&
                (this.refreshToken == null || this.refreshToken.isBlank());
    }
}
