package com.example.vaccinationsystem.dto;

import java.util.List;
import java.util.Map;

public class StatisticsDTO {
    private double totalRevenue;
    private int todayBillsCount;
    private double todayRevenue;

    private List<Map<String, Object>> topVaccines;
    private List<Map<String, Object>> upcomingVaccinations;
    private List<Map<String, Object>> lowStockVaccines;

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTodayBillsCount() {
        return todayBillsCount;
    }

    public void setTodayBillsCount(int todayBillsCount) {
        this.todayBillsCount = todayBillsCount;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public List<Map<String, Object>> getTopVaccines() {
        return topVaccines;
    }

    public void setTopVaccines(List<Map<String, Object>> topVaccines) {
        this.topVaccines = topVaccines;
    }

    public List<Map<String, Object>> getUpcomingVaccinations() {
        return upcomingVaccinations;
    }

    public void setUpcomingVaccinations(List<Map<String, Object>> upcomingVaccinations) {
        this.upcomingVaccinations = upcomingVaccinations;
    }

    public List<Map<String, Object>> getLowStockVaccines() {
        return lowStockVaccines;
    }

    public void setLowStockVaccines(List<Map<String, Object>> lowStockVaccines) {
        this.lowStockVaccines = lowStockVaccines;
    }
}
