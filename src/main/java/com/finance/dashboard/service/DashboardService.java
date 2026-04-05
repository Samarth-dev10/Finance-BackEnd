package com.finance.dashboard.service;

import com.finance.dashboard.model.DashboardFilterRq;
import com.finance.dashboard.model.DashboardRs;
import com.finance.model.FinResponse;

public interface DashboardService {

    FinResponse<DashboardRs> getSummary(DashboardFilterRq filterRq);

}
