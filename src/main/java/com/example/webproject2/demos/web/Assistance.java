package com.example.webproject2.demos.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assistance")
@Getter
@Setter
public class Assistance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assistanceId;

    @Column(name = "publicity_id", nullable = false)
    private Integer publicityId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false)
    private Integer status;
}
