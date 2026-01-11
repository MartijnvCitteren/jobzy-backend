package com.jobly_jobs.factory;

import com.jobly_jobs.domain.dto.request.GeneralJobDescriptionInfoDto;
import com.jobly_jobs.domain.enums.FunctionGroup;
import java.math.BigDecimal;

public class GeneralJobInfoDtoFactory {

  public static GeneralJobDescriptionInfoDto.GeneralJobDescriptionInfoDtoBuilder createGeneralInfoDto() {
    return GeneralJobDescriptionInfoDto.builder()
        .jobTitle("Software Engineer")
        .functionGroup(FunctionGroup.valueOf("ENGINEERING"))
        .companyName("Google")
        .minSalary(BigDecimal.valueOf(100000))
        .maxSalary(BigDecimal.valueOf(150000));
  }
}
