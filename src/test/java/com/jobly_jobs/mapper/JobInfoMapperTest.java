package app.jobzy.mapper;

import static org.junit.jupiter.api.Assertions.*;

import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.request.BenefitsRequestDto;
import app.jobzy.domain.dto.request.ContactInfoVacancyRequestDto;
import app.jobzy.domain.dto.request.JobInfoRequestDto;
import app.jobzy.domain.dto.request.WritingStyleRequestDto;
import app.jobzy.domain.enums.Language;
import app.jobzy.domain.enums.SalaryPeriod;
import app.jobzy.domain.enums.SeniorityLevel;
import app.jobzy.domain.enums.WritingStyle;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobInfoMapperTest {
  @InjectMocks
  JobInfoMapperImpl jobsInfoMapper;

  @Test
  @DisplayName("Given valid JobInfoRequestDto and UUID, when mapping to JobInfo, then all fields are correctly mapped")
  void givenValidJobInfoRequestDtoAndUuid_whenMappingToJobInfo_thenAllFieldsAreCorrectlyMapped() {
    // Given
    UUID companyInfoToken = UUID.randomUUID();

    WritingStyleRequestDto writingStyleDto = WritingStyleRequestDto.builder()
        .writingStyle(WritingStyle.BUSINESS_CASUAL)
        .language(Language.ENGLISH)
        .build();

    BenefitsRequestDto benefitsDto = BenefitsRequestDto.builder()
        .salaryPeriod(SalaryPeriod.YEARLY)
        .minSalary(new BigDecimal("50000.00"))
        .maxSalary(new BigDecimal("70000.00"))
        .extraPerks("Health insurance")
        .build();

    ContactInfoVacancyRequestDto contactInfoDto = new ContactInfoVacancyRequestDto(
        "John Doe",
        "john.doe@example.com",
        "+31612345678"
    );

    JobInfoRequestDto requestDto = JobInfoRequestDto.builder()
        .jobTitle("Senior Software Engineer")
        .seniorityLevel(SeniorityLevel.SENIOR)
        .jobSummary("Exciting opportunity to join our team")
        .tasks("Design and develop software")
        .skills("Java, Spring Boot, Microservices")
        .teamDescription("Dynamic team of 10 engineers")
        .writingStyle(writingStyleDto)
        .benefits(benefitsDto)
        .contactInfo(contactInfoDto)
        .build();

    // When
    JobInfo result = jobsInfoMapper.toJobInfo(requestDto, companyInfoToken);

    // Then
    assertEquals(companyInfoToken, result.companyInfoToken());
    assertEquals("Senior Software Engineer", result.jobTitle());
    assertEquals(SeniorityLevel.SENIOR, result.seniorityLevel());
    assertEquals("Exciting opportunity to join our team", result.jobSummary());
    assertEquals("Design and develop software", result.tasks());
    assertEquals("Java, Spring Boot, Microservices", result.skills());
    assertEquals("Dynamic team of 10 engineers", result.teamDescription());
    assertEquals(writingStyleDto, result.writingStyle());
    assertEquals(benefitsDto, result.benefits());
    assertEquals(contactInfoDto, result.contactInfo());
  }

}