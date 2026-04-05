package com.finance.dashboard.controller;

import com.finance.dashboard.model.DashboardFilterRq;
import com.finance.dashboard.model.DashboardRs;
import com.finance.dashboard.service.DashboardService;
import com.finance.model.FinResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    public ResponseEntity<FinResponse<DashboardRs>> getDashboardSummary(
            @ModelAttribute DashboardFilterRq filterRq) {

        return ResponseEntity.ok(dashboardService.getSummary(filterRq));
    }
}