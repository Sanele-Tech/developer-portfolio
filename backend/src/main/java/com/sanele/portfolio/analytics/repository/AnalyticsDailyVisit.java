package com.sanele.portfolio.analytics.repository;

import java.time.LocalDate;

public interface AnalyticsDailyVisit {
    LocalDate getVisitDate();
    long getVisits();
}
