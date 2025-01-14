package org.example.playus.domain.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequestDtoForUser {

    private String characterId;       // 캐릭터 ID
    private String currentPassword;   // 현재 비밀번호 (검증용)
    private String newPassword;       // 새 비밀번호 (변경용)

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}