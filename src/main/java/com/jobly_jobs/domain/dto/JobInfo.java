package com.jobly_jobs.domain.dto;

import com.jobly_jobs.domain.dto.request.BenefitsRequestDto;
import com.jobly_jobs.domain.dto.request.ContactInfoVacancyRequestDto;
import com.jobly_jobs.domain.dto.request.WritingStyleRequestDto;
import com.jobly_jobs.domain.enums.SeniorityLevel;
import java.util.UUID;
import lombok.Builder;

//formatter:off
@Builder
public record JobInfo(
    UUID companyInfoToken,
    String jobTitle,
    SeniorityLevel seniorityLevel,
    String jobSummary,
    String tasks,
    String skills,
    String teamDescription,
    WritingStyleRequestDto writingStyle,
    BenefitsRequestDto benefits,
    ContactInfoVacancyRequestDto contactInfo
) {

}
