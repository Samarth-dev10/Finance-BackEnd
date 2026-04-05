package com.finance.dashboard.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardRs {

    // Summary Totals

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;

    private BigDecimal netBalance;
    private long totalTransactionCount;

    // Category Breakdown
    private List<CategoryTotalRs> categoryBreakdown;

    // Monthly Trends
    private List<MonthlyTrendRs> monthlyTrends;

    // Recent Transactions
    private List<RecentTransactionRs> recentTransactions;

    // Applied Filter Context

    private String appliedStartDate;
    private String appliedEndDate;

    // INNER RESPONSE SHAPES
    @Data
    @Builder
    public static class CategoryTotalRs {
        private String type;      // "INCOME" or "EXPENSE"
        private String category;
        private BigDecimal total;
    }

    @Data
    @Builder
    public static class MonthlyTrendRs {
        private int year;
        private int month;
        private String monthName;
        private BigDecimal incomeTotal;
        private BigDecimal expenseTotal;
    }

    @Data
    @Builder
    public static class RecentTransactionRs {
        private Long id;
        private BigDecimal amount;
        private String type;       // "INCOME" or "EXPENSE"
        private String category;
        private String date;
        private String notes;
    }
}