package org.example.playus.domain.user.dto;

import lombok.Getter;

@Getter
public class UserUpdateCharacterRequestDtoUser {

    private String characterId;

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }
}
