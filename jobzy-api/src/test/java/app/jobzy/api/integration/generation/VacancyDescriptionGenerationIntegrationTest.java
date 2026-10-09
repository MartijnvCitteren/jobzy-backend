package app.jobzy.api.integration.generation;

import static app.jobzy.api.integration.IntegrationCommons.JSON_MAPPER;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.jobzy.api.adapter.out.persistence.generation.VacancyDescriptionGenerationJpaRepository;
import app.jobzy.api.adapter.out.persistence.vacancy.VacancyDescriptionJpaRepository;
import app.jobzy.api.adapter.out.persistence.vacancy.VacancyJpaRepository;
import app.jobzy.api.application.port.in.PurgeExpiredGenerationsUseCase;
import app.jobzy.api.application.port.out.GenerationResult;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.domain.generation.VacancyDescriptionGeneration;
import app.jobzy.api.domain.generation.valueobject.GeneratedDraft;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.domain.vacancy.valueobject.Language;
import app.jobzy.api.integration.BaseIntegrationTest;
import app.jobzy.api.vacancy.adapter.in.web.contract.ProblemDetails;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGenerationStatus;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Exercises starting and polling a description generation end-to-end through the real REST,
 * application and persistence layers. The generator port is mocked and {@code @Async} runs
 * synchronously, so the generation has finished when the start request returns.
 */
class VacancyDescriptionGenerationIntegrationTest extends BaseIntegrationTest {

  private static final String PROBLEM_JSON = "application/problem+json";
  private static final String VALID_REQUEST =
      """
      {
        "mostImportantTasks": "Visiting customers and closing deals",
        "team": "Five account managers and a sales lead",
        "whyNiceJob": "A lot of freedom and a great product"
      }
      """;
  private static final GeneratedDraft DRAFT =
      new GeneratedDraft("A summary", "A job description", "- Task one\n- Task two");

  @Autowired private VacancyJpaRepository vacancyJpaRepository;
  @Autowired private VacancyDescriptionJpaRepository vacancyDescriptionJpaRepository;
  @Autowired private VacancyDescriptionGenerationJpaRepository generationJpaRepository;
  @Autowired private VacancyDescriptionGenerationRepository generationRepository;
  @Autowired private PurgeExpiredGenerationsUseCase purgeExpiredGenerationsUseCase;
  @Autowired private Clock clock;

  @BeforeEach
  void setUp() {
    generationJpaRepository.deleteAll();
    vacancyDescriptionJpaRepository.deleteAll();
    vacancyJpaRepository.deleteAll();
  }

  @Test
  @DisplayName(
      "given an existing vacancy, when a generation is started and polled then the draft is"
          + " returned, built from the vacancy's job title and language, and the vacancy itself is"
          + " left untouched")
  void givenExistingVacancyWhenGenerationStartedAndPolledThenDraftReturnedAndVacancyUntouched() {
    var vacancyId = createVacancy();
    when(generator.generate(any()))
        .thenReturn(new GenerationResult.Drafted(DRAFT, "mistral-small-2506", "test/v1"));

    var start =
        given()
            .contentType(ContentType.JSON)
            .body(VALID_REQUEST)
            .when()
            .post("/vacancy/{id}/generate-description", vacancyId)
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .extract();
    var started =
        JSON_MAPPER.readValue(
            start.asString(),
            app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGeneration.class);

    assertEquals(VacancyDescriptionGenerationStatus.PENDING, started.getStatus());
    assertTrue(
        start
            .header("Location")
            .endsWith(
                "/vacancy/" + vacancyId + "/generate-description/" + started.getGenerationId()));

    var polled = poll(vacancyId, started.getGenerationId());
    assertEquals(VacancyDescriptionGenerationStatus.COMPLETED, polled.getStatus());
    assertEquals(DRAFT.summary(), polled.getDescription().getSummary());
    assertEquals(DRAFT.jobDescription(), polled.getDescription().getJobDescription());
    assertEquals(DRAFT.tasks(), polled.getDescription().getTasks());
    assertNull(polled.getDescription().getWhatWeOffer());
    assertNull(polled.getDescription().getAboutUs());

    verify(generator)
        .generate(
            new GenerationInput(
                "Sales Manager",
                Language.NL,
                "Visiting customers and closing deals",
                "Five account managers and a sales lead",
                "A lot of freedom and a great product"));
    var stored = generationJpaRepository.findById(started.getGenerationId()).orElseThrow();
    assertEquals("mistral-small-2506", stored.getModel());
    assertEquals("test/v1", stored.getPromptVersion());
    assertTrue(vacancyDescriptionJpaRepository.findByVacancyId(vacancyId).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("failedGenerations")
  @DisplayName(
      "given a generation that failed, when polled then a problem response with the status for"
          + " that failure is returned")
  void givenFailedGenerationWhenPolledThenProblemWithMatchingStatus(
      GenerationFailureReason reason, HttpStatus expectedStatus) {
    var vacancyId = createVacancy();
    when(generator.generate(any())).thenReturn(new GenerationResult.Rejected(reason));
    var generationId = startGeneration(vacancyId);

    var problem = pollExpectingProblem(vacancyId, generationId, expectedStatus);

    assertEquals("Description generation failed", problem.getTitle());
    assertEquals(expectedStatus.value(), problem.getStatus());
  }

  private static Stream<Arguments> failedGenerations() {
    return Stream.of(
        arguments(GenerationFailureReason.INPUT_REJECTED, HttpStatus.UNPROCESSABLE_CONTENT),
        arguments(GenerationFailureReason.PROVIDER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR),
        arguments(GenerationFailureReason.TIMEOUT, HttpStatus.INTERNAL_SERVER_ERROR),
        arguments(GenerationFailureReason.INVALID_OUTPUT, HttpStatus.INTERNAL_SERVER_ERROR));
  }

  @Test
  @DisplayName(
      "given a draft containing raw HTML, when the generation completes then it fails with"
          + " invalid output and the poll returns 500")
  void givenDraftWithRawHtmlWhenGenerationCompletesThenFailsAndPollReturns500() {
    var vacancyId = createVacancy();
    var unsafeDraft = new GeneratedDraft("<script>alert(1)</script>", "Job", "Tasks");
    when(generator.generate(any()))
        .thenReturn(new GenerationResult.Drafted(unsafeDraft, "model", "test/v1"));
    var generationId = startGeneration(vacancyId);

    pollExpectingProblem(vacancyId, generationId, HttpStatus.INTERNAL_SERVER_ERROR);

    var stored = generationJpaRepository.findById(generationId).orElseThrow();
    assertEquals(GenerationFailureReason.INVALID_OUTPUT, stored.getFailureReason());
    assertNull(stored.getDraftSummary());
  }

  @Test
  @DisplayName(
      "given the generator throws, when the generation completes then it fails with a provider"
          + " error instead of staying pending")
  void givenGeneratorThrowsWhenGenerationCompletesThenFailsWithProviderError() {
    var vacancyId = createVacancy();
    when(generator.generate(any())).thenThrow(new IllegalStateException("boom"));
    var generationId = startGeneration(vacancyId);

    pollExpectingProblem(vacancyId, generationId, HttpStatus.INTERNAL_SERVER_ERROR);

    var stored = generationJpaRepository.findById(generationId).orElseThrow();
    assertEquals(GenerationStatus.FAILED, stored.getStatus());
    assertEquals(GenerationFailureReason.PROVIDER_ERROR, stored.getFailureReason());
  }

  @Test
  @DisplayName(
      "given a pending generation older than 90 seconds, when polled then it is reported as a"
          + " timeout with 500")
  void givenPendingGenerationOlderThan90SecondsWhenPolledThenReportedAsTimeout() {
    var vacancyId = createVacancy();
    var generation =
        VacancyDescriptionGeneration.start(
            vacancyId, generationInput(), LocalDateTime.now(clock).minusSeconds(91));
    generationRepository.save(generation);

    var problem =
        pollExpectingProblem(vacancyId, generation.getId(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertEquals(
        "The generation did not finish in time. Start a new generation.", problem.getDetail());
    var stored = generationJpaRepository.findById(generation.getId()).orElseThrow();
    assertEquals(GenerationStatus.PENDING, stored.getStatus());
  }

  @Test
  @DisplayName("given a pending generation within 90 seconds, when polled then it is PENDING")
  void givenRecentPendingGenerationWhenPolledThenPending() {
    var vacancyId = createVacancy();
    var generation =
        VacancyDescriptionGeneration.start(
            vacancyId, generationInput(), LocalDateTime.now(clock).minusSeconds(30));
    generationRepository.save(generation);

    var polled = poll(vacancyId, generation.getId());

    assertEquals(VacancyDescriptionGenerationStatus.PENDING, polled.getStatus());
    assertNull(polled.getDescription());
  }

  @Test
  @DisplayName("given an unknown vacancy, when a generation is started then 404 and no call")
  void givenUnknownVacancyWhenGenerationStartedThen404AndNoGeneratorCall() {
    given()
        .contentType(ContentType.JSON)
        .body(VALID_REQUEST)
        .when()
        .post("/vacancy/{id}/generate-description", UUID.randomUUID())
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(PROBLEM_JSON);

    verify(generator, never()).generate(any());
    assertEquals(0, generationJpaRepository.count());
  }

  @Test
  @DisplayName("given an unknown generation id, when polled then 404")
  void givenUnknownGenerationIdWhenPolledThen404() {
    var vacancyId = createVacancy();

    pollExpectingProblem(vacancyId, UUID.randomUUID(), HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("given a generation of another vacancy, when polled via this vacancy's URL then 404")
  void givenGenerationOfAnotherVacancyWhenPolledViaThisVacancyThen404() {
    var vacancyId = createVacancy();
    var otherVacancyId = createVacancy();
    when(generator.generate(any()))
        .thenReturn(new GenerationResult.Drafted(DRAFT, "model", "test/v1"));
    var generationId = startGeneration(otherVacancyId);

    pollExpectingProblem(vacancyId, generationId, HttpStatus.NOT_FOUND);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidRequests")
  @DisplayName(
      "given an invalid answer, when a generation is started then 400 naming the field and"
          + " nothing is started")
  void givenInvalidAnswerWhenGenerationStartedThen400NamingTheField(
      String violation, String expectedField, String requestBody) {
    var vacancyId = createVacancy();

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/vacancy/{id}/generate-description", vacancyId)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(PROBLEM_JSON)
            .extract()
            .asString();
    var problem = JSON_MAPPER.readValue(response, ProblemDetails.class);

    assertEquals(expectedField, problem.getErrors().getFirst().getField());
    assertEquals(0, generationJpaRepository.count());
    verify(generator, never()).generate(any());
  }

  private static Stream<Arguments> invalidRequests() {
    return Stream.of(
        arguments(
            "team contains a script tag",
            "team",
            "{\"mostImportantTasks\": \"Sell\", \"team\": \"<script>x</script>\","
                + " \"whyNiceJob\": \"Fun\"}"),
        arguments(
            "whyNiceJob contains a control character",
            "whyNiceJob",
            "{\"mostImportantTasks\": \"Sell\", \"team\": \"Five\", \"whyNiceJob\":"
                + " \"Fun\\u0007\"}"),
        arguments(
            "mostImportantTasks exceeds 1000 characters",
            "mostImportantTasks",
            "{\"mostImportantTasks\": \""
                + "a".repeat(1001)
                + "\", \"team\": \"Five\", \"whyNiceJob\": \"Fun\"}"),
        arguments(
            "team is missing",
            "team",
            "{\"mostImportantTasks\": \"Sell\", \"whyNiceJob\": \"Fun\"}"));
  }

  @Test
  @DisplayName(
      "given generations older and younger than the retention period, when the clean-up runs then"
          + " only the old ones are deleted")
  void givenOldAndRecentGenerationsWhenPurgeRunsThenOnlyOldOnesDeleted() {
    var vacancyId = createVacancy();
    var now = LocalDateTime.now(clock);
    var expired =
        VacancyDescriptionGeneration.start(vacancyId, generationInput(), now.minusHours(33));
    var recent =
        VacancyDescriptionGeneration.start(vacancyId, generationInput(), now.minusHours(31));
    generationRepository.save(expired);
    generationRepository.save(recent);

    var deleted = purgeExpiredGenerationsUseCase.purgeExpired();

    assertEquals(1, deleted);
    assertTrue(generationJpaRepository.findById(expired.getId()).isEmpty());
    assertTrue(generationJpaRepository.findById(recent.getId()).isPresent());
    assertTrue(vacancyJpaRepository.findById(vacancyId).isPresent());
  }

  private static GenerationInput generationInput() {
    return new GenerationInput("Sales Manager", Language.NL, "Sell", "Five people", "Freedom");
  }

  private UUID createVacancy() {
    var requestBody =
        """
        {
          "jobTitle": "Sales Manager",
          "language": "nl",
          "category": "SALES",
          "location": {"country": "NL", "city": "Amsterdam"},
          "workplaceType": "HYBRID",
          "minHoursPerWeek": 32,
          "maxHoursPerWeek": 40
        }
        """;
    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/vacancy")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .asString();
    return JSON_MAPPER.readValue(response, VacancyResponse.class).getId();
  }

  private UUID startGeneration(UUID vacancyId) {
    var response =
        given()
            .contentType(ContentType.JSON)
            .body(VALID_REQUEST)
            .when()
            .post("/vacancy/{id}/generate-description", vacancyId)
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
            .extract()
            .asString();
    return JSON_MAPPER
        .readValue(
            response,
            app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGeneration.class)
        .getGenerationId();
  }

  private app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGeneration poll(
      UUID vacancyId, UUID generationId) {
    var response =
        given()
            .when()
            .get("/vacancy/{id}/generate-description/{generationId}", vacancyId, generationId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .asString();
    return JSON_MAPPER.readValue(
        response, app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGeneration.class);
  }

  private ProblemDetails pollExpectingProblem(
      UUID vacancyId, UUID generationId, HttpStatus expectedStatus) {
    var response =
        given()
            .when()
            .get("/vacancy/{id}/generate-description/{generationId}", vacancyId, generationId)
            .then()
            .statusCode(expectedStatus.value())
            .contentType(PROBLEM_JSON)
            .extract()
            .asString();
    return JSON_MAPPER.readValue(response, ProblemDetails.class);
  }
}
