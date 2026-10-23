package com.findsomeone.repository;

import com.findsomeone.entity.SafetyReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {
}
