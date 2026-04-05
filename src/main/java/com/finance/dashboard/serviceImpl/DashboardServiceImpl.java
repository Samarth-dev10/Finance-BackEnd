package com.finance.dashboard.serviceImpl;

import com.finance.core.utility.Utility;
import com.finance.dashboard.model.DashboardFilterRq;
import com.finance.dashboard.model.DashboardRs;
import com.finance.dashboard.service.DashboardService;
import com.finance.model.FinResponse;
import com.finance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;

        if (value instanceof BigDecimal bd) return bd;

        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public FinResponse<DashboardRs> getSummary(DashboardFilterRq filterRq) {

        try {
            LocalDate startDate = filterRq != null ? filterRq.getStartDate() : null;
            LocalDate endDate   = filterRq != null ? filterRq.getEndDate()   : null;

            // =========================
            // 1. TOTALS
            // =========================
            BigDecimal totalIncome =
                    Optional.ofNullable(transactionRepository
                                    .sumIncomeByDateRange(startDate, endDate))
                            .orElse(BigDecimal.ZERO);

            BigDecimal totalExpenses =
                    Optional.ofNullable(transactionRepository
                                    .sumExpenseByDateRange(startDate, endDate))
                            .orElse(BigDecimal.ZERO);

            BigDecimal netBalance = totalIncome.subtract(totalExpenses);

            long totalCount = Optional.ofNullable(
                    transactionRepository.countActiveTransactions()
            ).orElse(0L);

            // =========================
            // 2. CATEGORY BREAKDOWN
            // =========================
            List<Object[]> rawCategories =
                    Optional.ofNullable(
                            transactionRepository.getCategoryTotals(startDate, endDate)
                    ).orElse(Collections.emptyList());

            List<DashboardRs.CategoryTotalRs> categoryBreakdown =
                    rawCategories.stream()
                            .filter(Objects::nonNull)
                            .map(row -> {
                                try {
                                    return DashboardRs.CategoryTotalRs.builder()
                                            .type(row[0] != null ? row[0].toString() : "UNKNOWN")
                                            .category(row[1] != null ? row[1].toString() : "UNKNOWN")
                                            .total(toBigDecimal(row[2]))
                                            .build();
                                } catch (Exception e) {
                                    log.error("Category mapping error: {}", Arrays.toString(row), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .toList();

            // =========================
            // 3. MONTHLY TRENDS
            // =========================
            List<Object[]> rawMonthly =
                    Optional.ofNullable(
                            transactionRepository.getMonthlyTrends(startDate, endDate)
                    ).orElse(Collections.emptyList());

            Map<String, DashboardRs.MonthlyTrendRs.MonthlyTrendRsBuilder> monthMap =
                    new LinkedHashMap<>();

            for (Object[] row : rawMonthly) {

                try {
                    if (row == null) continue;

                    int year = row[0] != null ? ((Number) row[0]).intValue() : 0;
                    int month = row[1] != null ? ((Number) row[1]).intValue() : 1;

                    String type = row[2] != null ? row[2].toString() : "UNKNOWN";
                    BigDecimal amount = toBigDecimal(row[3]);

                    if (month < 1 || month > 12) {
                        log.warn("Invalid month from DB: {}", month);
                        continue;
                    }

                    String key = year + "-" + String.format("%02d", month);

                    monthMap.putIfAbsent(key,
                            DashboardRs.MonthlyTrendRs.builder()
                                    .year(year)
                                    .month(month)
                                    .monthName(Month.of(month)
                                            .getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                                    .incomeTotal(BigDecimal.ZERO)
                                    .expenseTotal(BigDecimal.ZERO)
                    );

                    var builder = monthMap.get(key);

                    if ("INCOME".equalsIgnoreCase(type)) {
                        builder.incomeTotal(amount);
                    } else if ("EXPENSE".equalsIgnoreCase(type)) {
                        builder.expenseTotal(amount);
                    }

                } catch (Exception e) {
                    log.error("Monthly mapping error: {}", Arrays.toString(row), e);
                }
            }

            List<DashboardRs.MonthlyTrendRs> monthlyTrends =
                    monthMap.values()
                            .stream()
                            .map(DashboardRs.MonthlyTrendRs.MonthlyTrendRsBuilder::build)
                            .toList();

            // =========================
            // 4. RECENT TRANSACTIONS
            // =========================
            var page = transactionRepository.findAllWithFilters(
                    null, null, null, null, null,
                    PageRequest.of(0, 5)
            );

            List<DashboardRs.RecentTransactionRs> recentTransactions =
                    Optional.ofNullable(page.getContent())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(tx -> DashboardRs.RecentTransactionRs.builder()
                                    .id(tx.getId())
                                    .amount(tx.getAmount())
                                    .type(tx.getType() != null ? tx.getType().name() : "UNKNOWN")
                                    .category(tx.getCategory())
                                    .date(tx.getDate() != null ? tx.getDate().toString() : null)
                                    .notes(tx.getNotes())
                                    .build())
                            .toList();

            // =========================
            // FINAL RESPONSE
            // =========================
            DashboardRs dashboard = DashboardRs.builder()
                    .totalIncome(totalIncome)
                    .totalExpenses(totalExpenses)
                    .netBalance(netBalance)
                    .totalTransactionCount(totalCount)
                    .categoryBreakdown(categoryBreakdown)
                    .monthlyTrends(monthlyTrends)
                    .recentTransactions(recentTransactions)
                    .appliedStartDate(startDate != null ? startDate.toString() : null)
                    .appliedEndDate(endDate != null ? endDate.toString() : null)
                    .build();

            return FinResponse.success("Dashboard summary fetched successfully", dashboard);

        } catch (Exception ex) {
            log.error("CRITICAL ERROR in DashboardService: ", ex); // <-- full stacktrace
            FinResponse<DashboardRs> response = new FinResponse<>();
            response.addError(Utility.internalError(ex.getMessage())); // expose real error
            return response;
        }
    }
}