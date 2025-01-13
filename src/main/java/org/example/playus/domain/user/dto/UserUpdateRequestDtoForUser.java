package org.example.playus.domain.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequestDtoForUser {

    private String characterId; // 캐릭터 ID
    private String password;    // 비밀번호 변경용

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getUpdatedPassword() {return this.password;}
}