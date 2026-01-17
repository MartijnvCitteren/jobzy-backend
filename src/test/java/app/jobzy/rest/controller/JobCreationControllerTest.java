package app.jobzy.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import app.jobzy.domain.dto.request.BenefitsRequestDto;
import app.jobzy.domain.dto.request.CompanyInfoRequestDto;
import app.jobzy.domain.dto.request.ContactInfoVacancyRequestDto;
import app.jobzy.domain.dto.request.JobInfoRequestDto;
import app.jobzy.domain.dto.request.WritingStyleRequestDto;
import app.jobzy.domain.dto.response.CompanyInfoResponseToken;
import app.jobzy.domain.dto.response.GeneratedVacancyDto;
import app.jobzy.domain.enums.Country;
import app.jobzy.domain.enums.Language;
import app.jobzy.domain.enums.SalaryPeriod;
import app.jobzy.domain.enums.SeniorityLevel;
import app.jobzy.domain.enums.WritingStyle;
import app.jobzy.service.CompanyInfoRetrievalService;
import app.jobzy.service.VacancyCreationService;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(JobCreationController.class)
@AutoConfigureMockMvc
class JobCreationControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  private CompanyInfoRetrievalService companyInfoService;

  private static final HttpHeaders HEADERS = new HttpHeaders();
  static {
    HEADERS.add("requestId", UUID.randomUUID().toString());
  }
  @MockitoBean
  private VacancyCreationService vacancyCreationService;


  @ParameterizedTest
  @MethodSource("provideValidCompanyInfoRequests")
  @DisplayName("Given valid company info with various URL formats, when sending company info, then returns created")
  void givenValidCompanyInfo_whenCreateCompanyInfo_thenReturnsStatusCreated(CompanyInfoRequestDto request,
      String testDescription) throws Exception {
    // given
    var expectedResponse = new CompanyInfoResponseToken("test-token-123");
    when(companyInfoService.getCompanyInfoResponseToken(any(CompanyInfoRequestDto.class))).thenReturn(
        expectedResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
            .contentType("application/json")
            .content(convertToJsonString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("test-token-123"));

    verify(companyInfoService, times(1)).getCompanyInfoResponseToken(any(CompanyInfoRequestDto.class));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidCompanyInfoRequests")
  @DisplayName("Given invalid company info with validation errors, when sending company info, then returns bad " +
      "request")
  void givenInvalidCompanyInfo_whenCreateCompanyInfo_thenReturnsBadRequest(CompanyInfoRequestDto request,
      String expectedField,
      String expectedMessagePart)
      throws Exception {
    // when & then
    mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
            .contentType("application/json")
            .content(convertToJsonString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$." + expectedField).exists())
        .andExpect(jsonPath("$." + expectedField).value(
            org.hamcrest.Matchers.containsString(expectedMessagePart)));

    verifyNoInteractions(companyInfoService);
  }

  @ParameterizedTest
  @MethodSource("provideValidJobInfoRequests")
  @DisplayName("Given valid job info request, when creating vacancy text, then returns created")
  void givenValidJobInfo_whenCreateVacancyText_thenReturnsStatusCreated(JobInfoRequestDto request,
      String testDescription) throws Exception {
    // given
    UUID requestId = UUID.randomUUID();
    GeneratedVacancyDto expectedResponse = GeneratedVacancyDto.builder()
        .summary("Generated summary")
        .companyDescription("Company description")
        .build();
    when(vacancyCreationService.createVacancy(any(JobInfoRequestDto.class), any(UUID.class)))
        .thenReturn(expectedResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.post("/create-vacancy")
            .param("requestId", requestId.toString())
            .headers(HEADERS)
            .contentType("application/json")
            .content(convertToJsonString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.summary").value("Generated summary"))
        .andExpect(jsonPath("$.companyDescription").value("Company description"));

    verify(vacancyCreationService, times(1)).createVacancy(any(JobInfoRequestDto.class), any(UUID.class));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidJobInfoRequests")
  @DisplayName("Given invalid job info with validation errors, when creating vacancy text, then returns bad request")
  void givenInvalidJobInfo_whenCreateVacancyText_thenReturnsBadRequest(JobInfoRequestDto request,
      String expectedField,
      String expectedMessagePart)
      throws Exception {
    // given
    UUID requestId = UUID.randomUUID();

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.post("/create-vacancy")
            .param("requestId", requestId.toString())
            .contentType("application/json")
            .content(convertToJsonString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$." + expectedField).exists())
        .andExpect(jsonPath("$." + expectedField).value(
            org.hamcrest.Matchers.containsString(expectedMessagePart)));

    verifyNoInteractions(vacancyCreationService);
  }

  private static Stream<Arguments> provideValidCompanyInfoRequests() {
    return Stream.of(
        // Valid URLs with https://
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://techcorp.com", Country.THE_NETHERLANDS,
                "https://techcorp.com/careers"),
            "Valid URL with https and .com"),
        Arguments.of(
            new CompanyInfoRequestDto("TechCorp", "https://www.techcorp.nl", Country.THE_NETHERLANDS, null),
            "Valid URL with https, www and .nl"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "http://techcorp.be", Country.BELGIUM, null),
            "Valid URL with http and .be"),
        Arguments.of(
            new CompanyInfoRequestDto("TechCorp", "https://company-name.de/path/to/page", Country.GERMANY,
                null), "Valid URL with https, dash in domain and path"),
        // Valid URLs with www.
        Arguments.of(new CompanyInfoRequestDto("Corp", "www.example.com", Country.THE_NETHERLANDS, null),
            "Valid URL starting with www"), Arguments.of(
            new CompanyInfoRequestDto("Corp", "www.test-company.io", Country.THE_NETHERLANDS,
                "www.test-company.io/jobs"), "Valid URL with www, dash and .io"),
        // Various TLDs
        Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.eu", Country.THE_NETHERLANDS, null),
            "Valid URL with .eu TLD"),
        Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.tech", Country.THE_NETHERLANDS, null),
            "Valid URL with .tech TLD"),
        Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.co.uk", Country.THE_NETHERLANDS, null),
            "Valid URL with .co.uk TLD"),
        // Minimal and maximal valid lengths
        Arguments.of(new CompanyInfoRequestDto("C", "www.a.com", Country.BELGIUM, null),
            "Minimal valid lengths"), Arguments.of(
            new CompanyInfoRequestDto("A".repeat(50), "https://techcorp.com", Country.GERMANY,
                "https://techcorp.com/jobs"), "Maximal valid lengths"));
  }

  private static Stream<Arguments> provideInvalidCompanyInfoRequests() {
    return Stream.of(
        // Company name validation
        Arguments.of(new CompanyInfoRequestDto("", "https://test.com", Country.THE_NETHERLANDS, null),
            "companyName", "Company name must be between 1 and 50 characters"), Arguments.of(
            new CompanyInfoRequestDto("A".repeat(51), "https://test.com", Country.THE_NETHERLANDS, null),
            "companyName", "Company name must be between 1 and 50 characters"),

        // Website URL regex validation - invalid formats
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "not-a-url", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "ftp://test.com", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "test.com", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "www.test", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "http://test", Country.THE_NETHERLANDS, null),
            "companyWebsite", "website address should look like 'www.example.com'"),

        // Country validation
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://test.com", null, null), "country",
            "must not be null"),

        // Example vacancy URL regex validation
        Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS,
                "not-a-valid-url"), "exampleVacancyUrl",
            "Vacancy url should start with 'www.example.com'"), Arguments.of(
            new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS,
                "ftp://test.com"), "exampleVacancyUrl",
            "Vacancy url should start with 'www.example.com'"), Arguments.of(
            new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS, "test.xyz"),
            "exampleVacancyUrl", "Vacancy url should start with 'www.example.com'"));
  }

  private static Stream<Arguments> provideValidJobInfoRequests() {
    return Stream.of(
        Arguments.of(createValidJobInfoRequest(), "Complete valid request with all fields"),

        Arguments.of(JobInfoRequestDto.builder()
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
                .build(),
            "Minimal valid request"),

        Arguments.of(JobInfoRequestDto.builder()
                .jobTitle("A".repeat(75))
                .seniorityLevel(SeniorityLevel.SENIOR)
                .jobSummary("A".repeat(300))
                .tasks("A".repeat(300))
                .skills("A".repeat(300))
                .teamDescription("A".repeat(300))
                .writingStyle(WritingStyleRequestDto.builder()
                    .writingStyle(WritingStyle.CREATIVE)
                    .language(Language.DUTCH)
                    .build())
                .benefits(BenefitsRequestDto.builder()
                    .salaryPeriod(SalaryPeriod.MONTHLY)
                    .minSalary(new BigDecimal("999999.99"))
                    .maxSalary(new BigDecimal("999999.99"))
                    .extraPerks("Great benefits")
                    .build())
                .contactInfo(new ContactInfoVacancyRequestDto(
                    "A".repeat(25),
                    "test@example.com",
                    "+1234567890123"))
                .build(),
            "Maximal valid request")
    );
  }

  private static Stream<Arguments> provideInvalidJobInfoRequests() {
    return Stream.of(
        Arguments.of(createJobInfoWithInvalidField("jobTitle", "A"),
            "jobTitle", "Job Title must be between 2 and 75 characters"),
        Arguments.of(createJobInfoWithInvalidField("jobTitle", "A".repeat(76)),
            "jobTitle", "Job Title must be between 2 and 75 characters"),

        Arguments.of(createJobInfoWithInvalidField("seniorityLevel", null),
            "seniorityLevel", "must not be null"),

        Arguments.of(createJobInfoWithInvalidField("jobSummary", "A".repeat(19)),
            "jobSummary", "Summary must be between 20 and 300 characters"),
        Arguments.of(createJobInfoWithInvalidField("jobSummary", "A".repeat(301)),
            "jobSummary", "Summary must be between 20 and 300 characters"),

        Arguments.of(createJobInfoWithInvalidField("tasks", "A".repeat(9)),
            "tasks", "Tasks must be between 10 and 300 characters"),
        Arguments.of(createJobInfoWithInvalidField("tasks", "A".repeat(301)),
            "tasks", "Tasks must be between 10 and 300 characters"),

        Arguments.of(createJobInfoWithInvalidField("skills", "A".repeat(9)),
            "skills", "Skills must be between 10 and 300 characters"),
        Arguments.of(createJobInfoWithInvalidField("skills", "A".repeat(301)),
            "skills", "Skills must be between 10 and 300 characters"),

        Arguments.of(createJobInfoWithInvalidField("teamDescription", "A".repeat(9)),
            "teamDescription", "Team description must be between 10 and 300 characters"),
        Arguments.of(createJobInfoWithInvalidField("teamDescription", "A".repeat(301)),
            "teamDescription", "Team description must be between 10 and 300 characters"),

        Arguments.of(createJobInfoWithInvalidField("writingStyle", null),
            "writingStyle", "must not be null")
    );
  }

  private static JobInfoRequestDto createValidJobInfoRequest() {
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

  private static JobInfoRequestDto createJobInfoWithInvalidField(String fieldName, Object value) {
    var builder = JobInfoRequestDto.builder()
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
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane", "jane@example.com", "+31612345678"));

    return switch (fieldName) {
      case "jobTitle" -> builder.jobTitle((String) value).build();
      case "seniorityLevel" -> builder.seniorityLevel((SeniorityLevel) value).build();
      case "jobSummary" -> builder.jobSummary((String) value).build();
      case "tasks" -> builder.tasks((String) value).build();
      case "skills" -> builder.skills((String) value).build();
      case "teamDescription" -> builder.teamDescription((String) value).build();
      case "writingStyle" -> builder.writingStyle((WritingStyleRequestDto) value).build();
      default -> builder.build();
    };
  }

  private static JobInfoRequestDto createJobInfoWithInvalidBenefits(SalaryPeriod salaryPeriod, BigDecimal minSalary) {
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
            .salaryPeriod(salaryPeriod)
            .minSalary(minSalary)
            .build())
        .contactInfo(new ContactInfoVacancyRequestDto("Jane", "jane@example.com", "+31612345678"))
        .build();
  }

  private String convertToJsonString(Object object) {
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    try {
      return objectWriter.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


}
