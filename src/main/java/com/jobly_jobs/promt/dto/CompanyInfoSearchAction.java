package com.jobly_jobs.promt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompanyInfoSearchAction extends Action {
    String companyName;
    String countryLocated;
    String urlExampleVacancy;

}
