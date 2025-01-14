package org.example.playus.domain.board;

import lombok.Getter;
import org.example.playus.global.Timestamped;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "boards")
public class Board extends Timestamped {

    @Id
    private String id; // MongoDB ObjectId
    private String title;  // 게시글 제목
    private String content;  // 게시글 내용
    private JobGroup jobGroup;

    public void setJobGroup(JobGroup jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
