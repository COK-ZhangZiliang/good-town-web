package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Integer> {

    @Query("SELECT new com.example.webproject2.demos.web.TownStatisticsDTO(" +
            "t.townId, t.townName, t.city, t.province, ms.month, " +
            "ms.totalPublicityUsers, ms.totalAssistanceUsers) " +
            "FROM MonthlySummary ms " +
            "JOIN ms.town t " +
            "WHERE (:province IS NULL OR t.province = :province) " +
            "AND (:city IS NULL OR t.city = :city) " +
            "AND ms.month BETWEEN :startMonth AND :endMonth " +
            "ORDER BY t.townId, ms.month")
    List<TownStatisticsDTO> findTownMonthlyStatistics(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth,
            @Param("province") String province,
            @Param("city") String city
    );
}