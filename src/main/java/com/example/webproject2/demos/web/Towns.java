package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "towns")
@Setter
@Getter
public class Towns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer townId;

    private String townName;
    private String city;
    private String province;
}
