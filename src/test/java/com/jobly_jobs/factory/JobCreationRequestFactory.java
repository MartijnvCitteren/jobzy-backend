package app.jobzy.factory;

import app.jobzy.domain.entity.JobCreationRequest;
import app.jobzy.domain.enums.FunctionGroup;
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
