package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class TownStatisticsController {

    @Autowired
    private TownStatisticsService townStatisticsService;

    @GetMapping("/towns")
    public List<TownStatisticsDTO> getTownStatistics(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "city", required = false) String city
    ) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 将空字符串转换为 null
        if (province != null && province.trim().isEmpty()) {
            province = null;
        }
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }

        return townStatisticsService.getTownStatistics(startDateTime, endDateTime, province, city);
    }
}