package org.example.playus.domain.level;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "levelExp")
public class Level {
    private String levelGroup;
    private List<LevelExp> levelExp;

    @Builder
    public Level(String levelGroup, List<LevelExp> levelExp) {
        this.levelGroup = levelGroup;
        this.levelExp = levelExp != null ? levelExp : new ArrayList<>();
    }
}
