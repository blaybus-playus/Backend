package org.example.playus.global;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Document
public abstract class Timestamped {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @CreatedDate
    @Field("created_at")
    private String createdAt;

    @LastModifiedDate
    @Field("modified_at")
    private String modifiedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FORMATTER);
        }
    }

    @PreUpdate
    public void onUpdate() {
        modifiedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FORMATTER);
    }
}

