package app.jobzy.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Checks the contract validation itself against the real vacancy contract. */
class OpenApiResponseValidationFilterTest {

  private static final String JSON = "application/json";
  private static final String PROBLEM_JSON = "application/problem+json";
  private static final String VACANCY_PATH = "/vacancy";
  private static final String DESCRIPTION_PATH =
      "/vacancy/01a11fe3-bbca-76a0-8e52-152e2272cba7/description";

  private static final String VALID_VACANCY =
      """
      {"id": "01a11fe3-bbca-76a0-8e52-152e2272cba7", "status": "DRAFT", "jobTitle": "Sales Manager",
       "category": "SALES", "location": {"country": "NL", "city": "Amsterdam"},
       "workplaceType": "HYBRID", "minHoursPerWeek": 32, "maxHoursPerWeek": 40,
       "createdAt": "2026-10-09T10:59:40.874128Z", "publishedAt": null}
      """;

  private static final String VALID_PROBLEM =
      """
      {"type": "about:blank", "title": "Not found", "status": 404,
       "instance": "http://localhost/api/v1/vacancy/01a11fe3-bbca-76a0-8e52-152e2272cba7"}
      """;

  private final OpenApiResponseValidationFilter filter =
      OpenApiResponseValidationFilter.vacancyApi();

  @Test
  @DisplayName("given a response that matches the contract, when validated, then it passes")
  void givenConformingResponseWhenValidatedThenNoViolations() {
    assertEquals(List.of(), filter.validate("POST", VACANCY_PATH, 201, JSON, VALID_VACANCY));
  }

  @Test
  @DisplayName(
      "given a problem response for a templated path, when validated, then the referenced response"
          + " and media type are used and it passes")
  void givenProblemResponseOnTemplatedPathWhenValidatedThenNoViolations() {
    assertEquals(
        List.of(),
        filter.validate(
            "POST", DESCRIPTION_PATH, 404, PROBLEM_JSON + ";charset=UTF-8", VALID_PROBLEM));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("violations")
  @DisplayName("given a response that violates the contract, when validated, then it is reported")
  void givenViolatingResponseWhenValidatedThenViolationReported(
      String violation,
      String method,
      String path,
      int status,
      String contentType,
      String body,
      String expectedMessagePart) {
    var violations = filter.validate(method, path, status, contentType, body);

    assertEquals(1, violations.size(), () -> "violations: " + violations);
    assertTrue(
        violations.getFirst().contains(expectedMessagePart),
        () -> "'%s' does not mention '%s'".formatted(violations.getFirst(), expectedMessagePart));
  }

  private static Stream<Arguments> violations() {
    return Stream.of(
        arguments(
            "required property missing",
            "POST",
            VACANCY_PATH,
            201,
            JSON,
            VALID_VACANCY.replace("\"jobTitle\": \"Sales Manager\",", ""),
            "jobTitle"),
        arguments(
            "undocumented property",
            "POST",
            VACANCY_PATH,
            201,
            JSON,
            VALID_VACANCY.replace("\"status\"", "\"email\": \"a@b.nl\", \"status\""),
            "email"),
        arguments(
            "null for a non-nullable property",
            "POST",
            VACANCY_PATH,
            201,
            JSON,
            VALID_VACANCY.replace("\"publishedAt\": null", "\"offer\": null"),
            "offer"),
        arguments(
            "invalid format",
            "POST",
            VACANCY_PATH,
            201,
            JSON,
            VALID_VACANCY.replace("01a11fe3-bbca-76a0-8e52-152e2272cba7", "not-a-uuid"),
            "/id"),
        arguments(
            "relative URI where the contract requires an absolute one",
            "POST",
            DESCRIPTION_PATH,
            404,
            PROBLEM_JSON,
            VALID_PROBLEM.replace("http://localhost", ""),
            "/instance"),
        arguments(
            "undocumented status code",
            "POST",
            VACANCY_PATH,
            418,
            JSON,
            VALID_VACANCY,
            "status 418"),
        arguments(
            "undocumented content type",
            "POST",
            VACANCY_PATH,
            400,
            JSON,
            VALID_PROBLEM,
            "content type 'application/json'"),
        arguments("undocumented path", "GET", "/candidate", 200, JSON, "{}", "path /candidate"),
        arguments(
            "undocumented method", "PUT", VACANCY_PATH, 200, JSON, "{}", "operation PUT /vacancy"),
        arguments(
            "body where the contract declares none",
            "DELETE",
            "/vacancy/01a11fe3-bbca-76a0-8e52-152e2272cba7",
            204,
            JSON,
            "{}",
            "declares no body"),
        arguments(
            "missing body where the contract declares one",
            "POST",
            VACANCY_PATH,
            201,
            JSON,
            "",
            "none was returned"));
  }
}
