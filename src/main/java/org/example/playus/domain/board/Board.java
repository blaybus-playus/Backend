package org.example.playus.domain.board;

import org.example.playus.global.Timestamped;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "boards")
public class Board extends Timestamped {

    @Id
    private String id; // MongoDB ObjectId
    private String title;  // 게시글 제목
    private String content;  // 게시글 내용

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
