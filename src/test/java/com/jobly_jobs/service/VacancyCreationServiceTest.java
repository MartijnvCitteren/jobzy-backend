package com.jobly_jobs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobly_jobs.agent.vacancy.BenefitAgent;
import com.jobly_jobs.agent.vacancy.CoreVacancyAgent;
import com.jobly_jobs.agent.vacancy.TeamAndCompanyAgent;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.BenefitsVacancyAiResponse;
import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.domain.dto.agent.CoreVacancyAiResponse;
import com.jobly_jobs.domain.dto.request.BenefitsRequestDto;
import com.jobly_jobs.domain.dto.request.ContactInfoVacancyRequestDto;
import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import com.jobly_jobs.domain.dto.request.WritingStyleRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.domain.enums.Language;
import com.jobly_jobs.domain.enums.SalaryPeriod;
import com.jobly_jobs.domain.enums.SeniorityLevel;
import com.jobly_jobs.domain.enums.WritingStyle;
import com.jobly_jobs.mapper.JobInfoMapper;
import com.jobly_jobs.mapper.VacancyMapper;
import com.jobly_jobs.prompt.dto.PromptValues;
import com.jobly_jobs.prompt.generator.vacancy.BenefitGenerator;
import com.jobly_jobs.prompt.generator.vacancy.CoreVacancyGenerator;
import com.jobly_jobs.prompt.generator.vacancy.TeamAndCompanyGenerator;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancyCreationServiceTest {

  @Mock
  private JobInfoMapper jobInfoMapper;

  @Mock
  private TeamAndCompanyGenerator teamAndCompanyGenerator;

  @Mock
  private CoreVacancyGenerator coreVacancyGenerator;

  @Mock
  private BenefitGenerator benefitGenerator;

  @Mock
  private TeamAndCompanyAgent teamAndCompanyAgent;

  @Mock
  private CoreVacancyAgent coreVacancyAgent;

  @Mock
  private BenefitAgent benefitAgent;

  @Mock
  private VacancyMapper vacancyMapper;

  @InjectMocks
  private VacancyCreationService vacancyCreationService;

  @Test
  @DisplayName("Given valid job info request, when creating vacancy, then returns generated vacancy dto")
  void givenValidJobInfoRequest_whenCreateVacancy_thenReturnsGeneratedVacancyDto() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createValidJobInfoRequest();
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Company Description",
        "Team Description"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Day to Day Description",
        "Job Description",
        "Unique Selling Points",
        "Requirements"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Offer",
        "Contact Information"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Summary")
        .companyDescription("Company Description")
        .teamDescription("Team Description")
        .dayToDayDescription("Day to Day Description")
        .jobDescription("Job Description")
        .jobUniqueSellingPoints("Unique Selling Points")
        .requirements("Requirements")
        .offer("Offer")
        .contactInformation("Contact Information")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    assertEquals(expectedVacancyDto, result);
    assertEquals("Summary", result.summary());
    assertEquals("Company Description", result.companyDescription());
    assertEquals("Team Description", result.teamDescription());
    assertEquals("Day to Day Description", result.dayToDayDescription());
    assertEquals("Job Description", result.jobDescription());
    assertEquals("Unique Selling Points", result.jobUniqueSellingPoints());
    assertEquals("Requirements", result.requirements());
    assertEquals("Offer", result.offer());
    assertEquals("Contact Information", result.contactInformation());

    verify(jobInfoMapper, times(1)).toJobInfo(jobInfoDto, companyToken);
    verify(teamAndCompanyGenerator, times(1)).getPrompt(jobInfo);
    verify(coreVacancyGenerator, times(1)).getPrompt(jobInfo);
    verify(benefitGenerator, times(1)).getPrompt(jobInfo);
    verify(teamAndCompanyAgent, times(1)).execute(teamAndCompanyPrompt);
    verify(coreVacancyAgent, times(1)).execute(corePrompt);
    verify(benefitAgent, times(1)).execute(benefitsPrompt);
    verify(vacancyMapper, times(1)).toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse);
  }

  @Test
  @DisplayName("Given minimal job info request, when creating vacancy, then returns generated vacancy dto")
  void givenMinimalJobInfoRequest_whenCreateVacancy_thenReturnsGeneratedVacancyDto() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createMinimalJobInfoRequest();
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Minimal Company Description",
        "Minimal Team Description"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Minimal Day to Day",
        "Minimal Job Description",
        "Minimal USP",
        "Minimal Requirements"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Minimal Offer",
        "Minimal Contact"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Minimal Summary")
        .companyDescription("Minimal Company Description")
        .teamDescription("Minimal Team Description")
        .dayToDayDescription("Minimal Day to Day")
        .jobDescription("Minimal Job Description")
        .jobUniqueSellingPoints("Minimal USP")
        .requirements("Minimal Requirements")
        .offer("Minimal Offer")
        .contactInformation("Minimal Contact")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    assertEquals(expectedVacancyDto, result);

    verify(jobInfoMapper, times(1)).toJobInfo(jobInfoDto, companyToken);
    verify(teamAndCompanyGenerator, times(1)).getPrompt(jobInfo);
    verify(coreVacancyGenerator, times(1)).getPrompt(jobInfo);
    verify(benefitGenerator, times(1)).getPrompt(jobInfo);
    verify(teamAndCompanyAgent, times(1)).execute(teamAndCompanyPrompt);
    verify(coreVacancyAgent, times(1)).execute(corePrompt);
    verify(benefitAgent, times(1)).execute(benefitsPrompt);
    verify(vacancyMapper, times(1)).toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse);
  }

  @Test
  @DisplayName("Given job info with Dutch language, when creating vacancy, then returns generated vacancy dto in Dutch")
  void givenJobInfoWithDutchLanguage_whenCreateVacancy_thenReturnsGeneratedVacancyDtoInDutch() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createJobInfoRequestWithLanguage(Language.DUTCH);
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Bedrijfsomschrijving",
        "Teamomschrijving"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Dagelijkse werkzaamheden",
        "Functieomschrijving",
        "Unieke voordelen",
        "Vereisten"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Aanbod",
        "Contactinformatie"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Samenvatting")
        .companyDescription("Bedrijfsomschrijving")
        .teamDescription("Teamomschrijving")
        .dayToDayDescription("Dagelijkse werkzaamheden")
        .jobDescription("Functieomschrijving")
        .jobUniqueSellingPoints("Unieke voordelen")
        .requirements("Vereisten")
        .offer("Aanbod")
        .contactInformation("Contactinformatie")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    assertEquals(expectedVacancyDto, result);

    verify(jobInfoMapper, times(1)).toJobInfo(jobInfoDto, companyToken);
    verify(teamAndCompanyAgent, times(1)).execute(teamAndCompanyPrompt);
    verify(coreVacancyAgent, times(1)).execute(corePrompt);
    verify(benefitAgent, times(1)).execute(benefitsPrompt);
  }

  @Test
  @DisplayName("Given job info with creative writing style, when creating vacancy, then returns creative vacancy dto")
  void givenJobInfoWithCreativeWritingStyle_whenCreateVacancy_thenReturnsCreativeVacancyDto() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createJobInfoRequestWithWritingStyle(WritingStyle.CREATIVE);
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Creative Company Description",
        "Creative Team Description"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Creative Day to Day",
        "Creative Job Description",
        "Creative USP",
        "Creative Requirements"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Creative Offer",
        "Creative Contact"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Creative Summary")
        .companyDescription("Creative Company Description")
        .teamDescription("Creative Team Description")
        .dayToDayDescription("Creative Day to Day")
        .jobDescription("Creative Job Description")
        .jobUniqueSellingPoints("Creative USP")
        .requirements("Creative Requirements")
        .offer("Creative Offer")
        .contactInformation("Creative Contact")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    assertEquals(expectedVacancyDto, result);

    verify(teamAndCompanyAgent, times(1)).execute(any());
    verify(coreVacancyAgent, times(1)).execute(any());
    verify(benefitAgent, times(1)).execute(any());
  }

  @Test
  @DisplayName("Given job info with senior level, when creating vacancy, then returns vacancy dto for senior position")
  void givenJobInfoWithSeniorLevel_whenCreateVacancy_thenReturnsVacancyDtoForSeniorPosition() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createJobInfoRequestWithSeniority(SeniorityLevel.SENIOR);
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Senior Company Description",
        "Senior Team Description"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Senior Day to Day",
        "Senior Job Description",
        "Senior USP",
        "Senior Requirements"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Senior Offer",
        "Senior Contact"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Senior Summary")
        .companyDescription("Senior Company Description")
        .teamDescription("Senior Team Description")
        .dayToDayDescription("Senior Day to Day")
        .jobDescription("Senior Job Description")
        .jobUniqueSellingPoints("Senior USP")
        .requirements("Senior Requirements")
        .offer("Senior Offer")
        .contactInformation("Senior Contact")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    assertEquals(expectedVacancyDto, result);

    verify(jobInfoMapper, times(1)).toJobInfo(jobInfoDto, companyToken);
    verify(teamAndCompanyGenerator, times(1)).getPrompt(jobInfo);
    verify(coreVacancyGenerator, times(1)).getPrompt(jobInfo);
    verify(benefitGenerator, times(1)).getPrompt(jobInfo);
  }

  @Test
  @DisplayName("Given job info, when creating vacancy, then all three agents execute asynchronously")
  void givenJobInfo_whenCreateVacancy_thenAllThreeAgentsExecuteAsynchronously() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createValidJobInfoRequest();
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Company", "Team"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Day to Day", "Job", "USP", "Reqs"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Offer", "Contact"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder()
        .summary("Summary")
        .companyDescription("Company")
        .teamDescription("Team")
        .dayToDayDescription("Day to Day")
        .jobDescription("Job")
        .jobUniqueSellingPoints("USP")
        .requirements("Reqs")
        .offer("Offer")
        .contactInformation("Contact")
        .build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    GeneratedVacancyDto result = vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    assertNotNull(result);
    verify(teamAndCompanyAgent, times(1)).execute(teamAndCompanyPrompt);
    verify(coreVacancyAgent, times(1)).execute(corePrompt);
    verify(benefitAgent, times(1)).execute(benefitsPrompt);
    verify(vacancyMapper, times(1)).toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse);
  }

  @Test
  @DisplayName("Given job info, when creating vacancy, then correct prompt values are generated for each agent")
  void givenJobInfo_whenCreateVacancy_thenCorrectPromptValuesAreGeneratedForEachAgent() {
    // given
    UUID companyToken = UUID.randomUUID();
    JobInfoRequestDto jobInfoDto = createValidJobInfoRequest();
    JobInfo jobInfo = createJobInfo(companyToken);

    PromptValues<JobInfo> teamAndCompanyPrompt = PromptValues.<JobInfo>builder().task("team-task").build();
    PromptValues<JobInfo> corePrompt = PromptValues.<JobInfo>builder().task("core-task").build();
    PromptValues<JobInfo> benefitsPrompt = PromptValues.<JobInfo>builder().task("benefits-task").build();

    CompanyAndTeamAiResponse teamAndCompanyResponse = new CompanyAndTeamAiResponse(
        "Company", "Team"
    );

    CoreVacancyAiResponse coreResponse = new CoreVacancyAiResponse(
        "Day to Day", "Job", "USP", "Reqs"
    );

    BenefitsVacancyAiResponse benefitResponse = new BenefitsVacancyAiResponse(
        "Offer", "Contact"
    );

    GeneratedVacancyDto expectedVacancyDto = GeneratedVacancyDto.builder().build();

    when(jobInfoMapper.toJobInfo(jobInfoDto, companyToken)).thenReturn(jobInfo);
    when(teamAndCompanyGenerator.getPrompt(jobInfo)).thenReturn(teamAndCompanyPrompt);
    when(coreVacancyGenerator.getPrompt(jobInfo)).thenReturn(corePrompt);
    when(benefitGenerator.getPrompt(jobInfo)).thenReturn(benefitsPrompt);
    when(teamAndCompanyAgent.execute(teamAndCompanyPrompt)).thenReturn(teamAndCompanyResponse);
    when(coreVacancyAgent.execute(corePrompt)).thenReturn(coreResponse);
    when(benefitAgent.execute(benefitsPrompt)).thenReturn(benefitResponse);
    when(vacancyMapper.toVacancyDto(teamAndCompanyResponse, coreResponse, benefitResponse))
        .thenReturn(expectedVacancyDto);

    // when
    vacancyCreationService.createVacancy(jobInfoDto, companyToken);

    // then
    verify(teamAndCompanyGenerator, times(1)).getPrompt(jobInfo);
    verify(coreVacancyGenerator, times(1)).getPrompt(jobInfo);
    verify(benefitGenerator, times(1)).getPrompt(jobInfo);
    verify(teamAndCompanyAgent, times(1)).execute(teamAndCompanyPrompt);
    verify(coreVacancyAgent, times(1)).execute(corePrompt);
    verify(benefitAgent, times(1)).execute(benefitsPrompt);
  }

  // Helper methods
  private JobInfoRequestDto createValidJobInfoRequest() {
    return JobInfoRequestDto.builder()
        .jobTitle("Senior Software Engineer")
        .seniorityLevel(SeniorityLevel.SENIOR)
        .jobSummary("We are looking for a talented engineer to join our team and work on exciting projects")
        .tasks("Design and develop software solutions, collaborate with team members")
        .skills("Java, Spring Boot, Microservices, REST APIs")
        .teamDescription("Dynamic team of 10 engineers working on innovative solutions")
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(WritingStyle.BUSINESS_CASUAL)
            .language(Language.ENGLISH)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(new BigDecimal("60000.00"))
            .maxSalary(new BigDecimal("80000.00"))
            .extraPerks("Health insurance, gym membership")
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane Doe", "jane.doe@example.com", "+31612345678"))
        .build();
  }

  private JobInfoRequestDto createMinimalJobInfoRequest() {
    return JobInfoRequestDto.builder()
        .jobTitle("SE")
        .seniorityLevel(SeniorityLevel.JUNIOR)
        .jobSummary("A".repeat(20))
        .tasks("A".repeat(10))
        .skills("A".repeat(10))
        .teamDescription("A".repeat(10))
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(WritingStyle.FORMAL)
            .language(Language.ENGLISH)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(BigDecimal.ZERO)
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto(null, null, null))
        .build();
  }

  private JobInfoRequestDto createJobInfoRequestWithLanguage(Language language) {
    return JobInfoRequestDto.builder()
        .jobTitle("Senior Software Engineer")
        .seniorityLevel(SeniorityLevel.SENIOR)
        .jobSummary("We are looking for a talented engineer to join our team and work on exciting projects")
        .tasks("Design and develop software solutions, collaborate with team members")
        .skills("Java, Spring Boot, Microservices, REST APIs")
        .teamDescription("Dynamic team of 10 engineers working on innovative solutions")
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(WritingStyle.BUSINESS_CASUAL)
            .language(language)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(new BigDecimal("60000.00"))
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane Doe", "jane.doe@example.com", "+31612345678"))
        .build();
  }

  private JobInfoRequestDto createJobInfoRequestWithWritingStyle(WritingStyle writingStyle) {
    return JobInfoRequestDto.builder()
        .jobTitle("Senior Software Engineer")
        .seniorityLevel(SeniorityLevel.SENIOR)
        .jobSummary("We are looking for a talented engineer to join our team and work on exciting projects")
        .tasks("Design and develop software solutions, collaborate with team members")
        .skills("Java, Spring Boot, Microservices, REST APIs")
        .teamDescription("Dynamic team of 10 engineers working on innovative solutions")
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(writingStyle)
            .language(Language.ENGLISH)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(new BigDecimal("60000.00"))
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane Doe", "jane.doe@example.com", "+31612345678"))
        .build();
  }

  private JobInfoRequestDto createJobInfoRequestWithSeniority(SeniorityLevel seniorityLevel) {
    return JobInfoRequestDto.builder()
        .jobTitle("Software Engineer")
        .seniorityLevel(seniorityLevel)
        .jobSummary("We are looking for a talented engineer to join our team and work on exciting projects")
        .tasks("Design and develop software solutions, collaborate with team members")
        .skills("Java, Spring Boot, Microservices, REST APIs")
        .teamDescription("Dynamic team of 10 engineers working on innovative solutions")
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(WritingStyle.BUSINESS_CASUAL)
            .language(Language.ENGLISH)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(new BigDecimal("60000.00"))
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane Doe", "jane.doe@example.com", "+31612345678"))
        .build();
  }

  private JobInfo createJobInfo(UUID companyToken) {
    return JobInfo.builder()
        .companyInfoToken(companyToken)
        .jobTitle("Senior Software Engineer")
        .seniorityLevel(SeniorityLevel.SENIOR)
        .jobSummary("We are looking for a talented engineer")
        .tasks("Design and develop")
        .skills("Java, Spring Boot")
        .teamDescription("Dynamic team")
        .writingStyle(WritingStyleRequestDto.builder()
            .writingStyle(WritingStyle.BUSINESS_CASUAL)
            .language(Language.ENGLISH)
            .build())
        .benefits(BenefitsRequestDto.builder()
            .salaryPeriod(SalaryPeriod.YEARLY)
            .minSalary(new BigDecimal("60000.00"))
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane", "jane@example.com", "+31612345678"))
        .build();
  }
}