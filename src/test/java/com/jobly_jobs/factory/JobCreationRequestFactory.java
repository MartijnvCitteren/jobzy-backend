package com.jobly_jobs.factory;

import com.jobly_jobs.domain.entity.JobCreationRequest;
import com.jobly_jobs.domain.enums.FunctionGroup;
import java.math.BigDecimal;

public class JobCreationRequestFactory {

  public static JobCreationRequest.JobCreationRequestBuilder createJobCreationRequest() {
    return JobCreationRequest.builder()
        .jobTitle("Software Engineer")
        .functionGroup(FunctionGroup.valueOf("ENGINEERING"))
        .companyName("Google")
        .minSalary(BigDecimal.valueOf(100000))
        .maxSalary(BigDecimal.valueOf(150000));
  }


}
