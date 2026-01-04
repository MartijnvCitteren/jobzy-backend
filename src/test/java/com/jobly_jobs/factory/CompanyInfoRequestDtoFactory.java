package com.jobly_jobs.factory;

import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.enums.Country;

public class CompanyInfoRequestDtoFactory {

  public static CompanyInfoRequestDto createCompanyInfoRequestDto() {
    return new CompanyInfoRequestDto("Tech Solutions BV", "www.techsolutions.com", Country.THE_NETHERLANDS,
        "www.techsolutions.com/careers/java-developer");
  }

  public static CompanyInfoRequestDto createCompanyInfoRequestDtoWithoutVacancyUrl() {
    return new CompanyInfoRequestDto("Tech Solutions BV", "www.techsolutions.com", Country.THE_NETHERLANDS, null);
  }
}

