package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.dto.StatisticsDTO;
import com.example.vaccinationsystem.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/summary")
    public StatisticsDTO getSummary(
            @RequestHeader(value = "X-Cashier-Id", required = false) String cashierId,
            @RequestHeader(value = "X-Username", required = false) String username
    ) {
        return statisticsService.getSummary(cashierId, username);
    }
}
