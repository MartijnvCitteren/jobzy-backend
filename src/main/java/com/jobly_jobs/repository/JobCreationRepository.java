package com.jobly_jobs.repository;

import com.jobly_jobs.domain.entity.JobCreationRequest;
import com.jobly_jobs.domain.enums.FunctionGroup;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCreationRepository extends JpaRepository<JobCreationRequest, Long> {

  Optional<JobCreationRequest> findByJobTitleAndFunctionGroupAndCompanyNameAndCreationDateAfter(String jobTitle,
      FunctionGroup functionGroup,
      String companyName,
      LocalDateTime createdAt);
}
