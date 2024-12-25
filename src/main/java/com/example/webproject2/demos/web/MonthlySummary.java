package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "monthly_summary")
@Getter
@Setter
public class MonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer summaryId;

    @Column(length = 6, nullable = false)
    private String month; // 格式为 YYYYMM

    @Column(length = 200, nullable = false)
    private String region; // 格式为 省-市

    @ManyToOne
    @JoinColumn(name = "town_id", nullable = false)
    private Towns town;

    @Column(length = 50, nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer totalPublicityUsers;

    @Column(nullable = false)
    private Integer totalAssistanceUsers;
}