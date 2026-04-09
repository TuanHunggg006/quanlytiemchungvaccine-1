package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dto.StatisticsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
    private final JdbcTemplate jdbcTemplate;

    public StatisticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public StatisticsDTO getSummary(String cashierId) {
        StatisticsDTO dto = new StatisticsDTO();

        dto.setTotalRevenue(getTotalRevenue());
        dto.setTodayBillsCount(getTodayBillsCount());
        dto.setTodayRevenue(getTodayRevenueByCashier(cashierId));

        dto.setTopVaccines(getTopVaccines());
        dto.setUpcomingVaccinations(getUpcomingVaccinations());

        // Quan trọng:
        // Trả về TẤT CẢ vaccine sắp hết / hết hàng
        // Không lọc theo INVENTORY_MANAGER_ID
        dto.setLowStockVaccines(getLowStockVaccines());

        return dto;
    }

    private double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(TOTAL_AMOUNT), 0) FROM BILL";
        Double value = jdbcTemplate.queryForObject(sql, Double.class);
        return value == null ? 0 : value;
    }

    private int getTodayBillsCount() {
        String sql = "SELECT COUNT(*) FROM BILL WHERE DUE_DATE = CURDATE()";
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private double getTodayRevenueByCashier(String cashierId) {
        if (cashierId == null || cashierId.isBlank()) {
            return 0;
        }

        String sql = """
            SELECT COALESCE(SUM(TOTAL_AMOUNT), 0)
            FROM BILL
            WHERE CASHIER_ID = ?
              AND DUE_DATE = CURDATE()
        """;

        Double value = jdbcTemplate.queryForObject(sql, Double.class, cashierId);
        return value == null ? 0 : value;
    }

    private java.util.List<java.util.Map<String, Object>> getTopVaccines() {
        String sql = """
            SELECT
                v.NAME AS NAME,
                COALESCE(SUM(d.CNT), 0) AS USAGE_COUNT
            FROM VACCINATION_FORM_DETAIL d
            JOIN VACCINE v ON d.VACCINE_ID = v.VACCINE_ID
            GROUP BY v.VACCINE_ID, v.NAME
            ORDER BY USAGE_COUNT DESC, v.NAME ASC
            LIMIT 5
        """;

        return jdbcTemplate.queryForList(sql);
    }

    private java.util.List<java.util.Map<String, Object>> getUpcomingVaccinations() {
        String sql = """
            SELECT
                c.NAME AS CUSTOMER_NAME,
                v.NAME AS VACCINE_NAME,
                d.DOSE AS DOSE,
                d.RETENTION AS RETENTION
            FROM VACCINATION_FORM_DETAIL d
            JOIN VACCINATION_FORM f ON d.VACCINATION_FORM_ID = f.VACCINATION_FORM_ID
            JOIN CUSTOMER c ON f.CUSTOMER_ID = c.CUSTOMER_ID
            JOIN VACCINE v ON d.VACCINE_ID = v.VACCINE_ID
            WHERE d.RETENTION BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
            ORDER BY d.RETENTION ASC, c.NAME ASC
        """;

        return jdbcTemplate.queryForList(sql);
    }

    private java.util.List<java.util.Map<String, Object>> getLowStockVaccines() {
        String sql = """
            SELECT
                VACCINE_ID AS VACCINE_ID,
                NAME AS NAME,
                QUANTITY_AVAILABLE AS QUANTITY_AVAILABLE
            FROM VACCINE
            WHERE QUANTITY_AVAILABLE <= 25
            ORDER BY QUANTITY_AVAILABLE ASC, NAME ASC
        """;

        return jdbcTemplate.queryForList(sql);
    }
}
