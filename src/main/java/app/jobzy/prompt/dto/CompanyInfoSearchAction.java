package app.jobzy.prompt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompanyInfoSearchAction extends Action {

  private String companyName;
  private String countryLocated;
  private String urlExampleVacancy;
}
