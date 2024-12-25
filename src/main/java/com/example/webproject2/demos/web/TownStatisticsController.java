package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class TownStatisticsController {

    @Autowired
    private TownStatisticsService townStatisticsService;

    @GetMapping("/towns")
    public List<TownStatisticsDTO> getTownStatistics(
            @RequestParam("startMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth startMonth,
            @RequestParam("endMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth endMonth,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "city", required = false) String city
    ) {
        // 将空字符串转换为 null
        if (province != null && province.trim().isEmpty()) {
            province = null;
        }
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }

        // 调用服务方法并直接传递 YearMonth
        return townStatisticsService.getTownStatistics(startMonth, endMonth, province, city);
    }
}
