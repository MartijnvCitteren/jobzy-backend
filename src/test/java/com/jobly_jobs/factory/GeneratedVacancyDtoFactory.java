package com.jobly_jobs.factory;

import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;

public class GeneratedVacancyDtoFactory {

  public static GeneratedVacancyDto.GeneratedVacancyDtoBuilder createGeneratedVacancyDto() {
    return GeneratedVacancyDto.builder()
        .summary("This is a job summary")
        .jobDescription("This is the job description")
        .dayToDayDescription("These are the tasks")
        .teamDescription("This is the team")
        .jobDescription("This is the job offer")
        .companyDescription("This is about the company");

  }
}
