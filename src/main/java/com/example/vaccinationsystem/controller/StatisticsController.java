package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestHeader(value = "X-Cashier-Id", required = false) String cashierId
    ) {
        return statisticsService.getSummary(cashierId);
    }
}
