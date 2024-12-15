package com.example.webproject2.demos.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assistance_success")
@Getter
@Setter
public class AssistanceSuccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer successId;

    @Column(name = "publicity_id", nullable = false)
    private Integer publicityId;

    @Column(name = "publicity_user_id", nullable = false)
    private Integer publicityUserId;

    @Column(name = "assistance_id", nullable = false)
    private Integer assistanceId;

    @Column(name = "assistance_user_id", nullable = false)
    private Integer assistanceUserId;

    @Column(name = "accepted_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;
}