package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TownStatisticsService {

    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;

    @Autowired
    private DataSource dataSource;

    /**
     * 执行 reset_monthly.sql 脚本
     */
    private void executeResetMonthlyScript() {
        Resource resource = new ClassPathResource("reset_monthly.sql");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
        } catch (SQLException e) {
            throw new RuntimeException("执行 reset_monthly.sql 脚本失败", e);
        }
    }

    /**
     * 获取乡镇统计数据
     */
    public List<TownStatisticsDTO> getTownStatistics(YearMonth startMonth, YearMonth endMonth, String province, String city) {
        // 在查询前执行 SQL 脚本
        executeResetMonthlyScript();

        System.out.println("已执行 reset_monthly.sql 脚本");

        // 将 YearMonth 转换为 YYYYMM 格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        String startMonthStr = startMonth.format(formatter);
        String endMonthStr = endMonth.format(formatter);

        // 执行查询
        return monthlySummaryRepository.findTownMonthlyStatistics(startMonthStr, endMonthStr, province, city);
    }

}