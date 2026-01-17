package app.jobzy.domain.dto;

import app.jobzy.domain.dto.request.BenefitsRequestDto;
import app.jobzy.domain.dto.request.ContactInfoVacancyRequestDto;
import app.jobzy.domain.dto.request.WritingStyleRequestDto;
import app.jobzy.domain.enums.SeniorityLevel;
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
