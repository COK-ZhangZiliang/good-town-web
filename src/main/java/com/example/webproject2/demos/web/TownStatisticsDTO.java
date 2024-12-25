package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TownStatisticsDTO {
    private Integer townId;
    private String townName;
    private String city;
    private String province;
    private String month; // 新增字段
    private Integer totalPublicityUsers; // 修改为 Integer
    private Integer totalAssistanceUsers; // 修改为 Integer

    // 构造函数
    public TownStatisticsDTO(Integer townId, String townName, String city, String province, String month, Integer totalPublicityUsers, Integer totalAssistanceUsers) {
        this.townId = townId;
        this.townName = townName;
        this.city = city;
        this.province = province;
        this.month = month;
        this.totalPublicityUsers = totalPublicityUsers;
        this.totalAssistanceUsers = totalAssistanceUsers;
    }
}
