package org.example.playus.domain.sheet;

import lombok.Getter;

@Getter
public class RangeReqeustDto {
    private String range;

    public RangeReqeustDto(String range) {
        this.range = range;
    }
}
