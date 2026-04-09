package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.StatisticsDao;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {
    private final StatisticsDao statisticsDao;

    public StatisticsService(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

    public Map<String, Object> getSummary(String cashierId) {
        Map<String, Object> result = new HashMap<>();

        result.put("totalRevenue", statisticsDao.getTotalRevenue());
        result.put("todayBillsCount", statisticsDao.getTodayBillsCount());
        result.put("topVaccines", statisticsDao.getTopVaccines());
        result.put("upcomingVaccinations", statisticsDao.getUpcomingVaccinations());
        result.put("lowStockVaccines", statisticsDao.getLowStockVaccines());
        result.put("todayRevenue", cashierId == null || cashierId.isBlank()
                ? 0
                : statisticsDao.getTodayRevenueByCashier(cashierId));

        return result;
    }
}
