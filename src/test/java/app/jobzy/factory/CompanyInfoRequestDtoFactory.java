package app.jobzy.factory;

import app.jobzy.domain.dto.request.CompanyInfoRequestDto;
import app.jobzy.domain.enums.Country;

public class CompanyInfoRequestDtoFactory {

  public static CompanyInfoRequestDto createCompanyInfoRequestDto() {
    return new CompanyInfoRequestDto("Tech Solutions BV", "www.techsolutions.com", Country.THE_NETHERLANDS,
        "www.techsolutions.com/careers/java-developer");
  }

  public static CompanyInfoRequestDto createCompanyInfoRequestDtoWithoutVacancyUrl() {
    return new CompanyInfoRequestDto("Tech Solutions BV", "www.techsolutions.com", Country.THE_NETHERLANDS, null);
  }
}

