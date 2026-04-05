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

    public StatisticsDTO getSummary() {
        StatisticsDTO stats = new StatisticsDTO();

        // 1. Admin Stats: Total Revenue
        String revSql = "SELECT SUM(TOTAL_AMOUNT) FROM BILL";
        Double totalRev = jdbcTemplate.queryForObject(revSql, Double.class);
        stats.setTotalRevenue(totalRev != null ? totalRev : 0.0);

        // 2. Admin Stats: Top Vaccines
        String topVacSql = """
                SELECT v.NAME, COUNT(*) as USAGE_COUNT
                FROM VACCINATION_FORM_DETAIL d
                JOIN VACCINE v ON v.VACCINE_ID = d.VACCINE_ID
                GROUP BY v.VACCINE_ID, v.NAME
                ORDER BY USAGE_COUNT DESC
                LIMIT 5
                """;
        stats.setTopVaccines(jdbcTemplate.queryForList(topVacSql));

        // 3. Doctor Stats: Upcoming vaccinations (Next dose reminders in next 7 days)
        String upcomingSql = """
                SELECT c.NAME as CUSTOMER_NAME, d.RETENTION, v.NAME as VACCINE_NAME, d.DOSE
                FROM VACCINATION_FORM_DETAIL d
                JOIN VACCINATION_FORM f ON f.VACCINATION_FORM_ID = d.VACCINATION_FORM_ID
                JOIN CUSTOMER c ON c.CUSTOMER_ID = f.CUSTOMER_ID
                JOIN VACCINE v ON v.VACCINE_ID = d.VACCINE_ID
                WHERE d.RETENTION BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)
                ORDER BY d.RETENTION ASC
                """;
        stats.setUpcomingVaccinations(jdbcTemplate.queryForList(upcomingSql));

        // 4. Inventory Stats: Low Stock
        String lowStockSql = """
                SELECT NAME, QUANTITY_AVAILABLE
                FROM VACCINE
                WHERE QUANTITY_AVAILABLE < 10
                ORDER BY QUANTITY_AVAILABLE ASC
                """;
        stats.setLowStockVaccines(jdbcTemplate.queryForList(lowStockSql));

        // 5. Cashier Stats: Today's Revenue
        String todayRevSql = "SELECT SUM(TOTAL_AMOUNT) FROM BILL WHERE DUE_DATE = CURRENT_DATE";
        Double todayRev = jdbcTemplate.queryForObject(todayRevSql, Double.class);
        stats.setTodayRevenue(todayRev != null ? todayRev : 0.0);

        // Due date = hạn thanh toán (thường không phải hôm nay); lấy thêm ngày tiêm trên phiếu
        // để số bill "hôm nay" khớp khi tạo bill cùng ngày tiêm nhưng due date sau.
        String todayCountSql = """
                SELECT COUNT(*) FROM BILL b
                JOIN VACCINATION_FORM f ON f.VACCINATION_FORM_ID = b.VACCINATION_FORM_ID
                WHERE b.DUE_DATE = CURRENT_DATE OR f.VACCINATION_DATE = CURRENT_DATE
                """;
        Long todayCount = jdbcTemplate.queryForObject(todayCountSql, Long.class);
        stats.setTodayBillsCount(todayCount != null ? todayCount : 0L);

        return stats;
    }
}
