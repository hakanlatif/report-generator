package com.example.reportgenerator.repository;

import com.example.reportgenerator.model.jpa.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {

    Optional<Report> findByJobId(String jobId);

}
