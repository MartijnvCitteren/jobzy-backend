package app.jobzy.api.integration.vacancy;

import static app.jobzy.api.integration.IntegrationCommons.JSON_MAPPER;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.jobzy.api.adapter.out.persistence.vacancy.VacancyJpaRepository;
import app.jobzy.api.integration.BaseIntegrationTest;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyStatus;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Runs against an in-memory H2 database so the vacancy is persisted through the real JPA adapter.
 */
class VacancyControllerIntegrationTest extends BaseIntegrationTest {
  @Autowired private VacancyJpaRepository vacancyJpaRepository;

  @BeforeEach
  void setUp() {
    vacancyJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /vacancy creates a vacancy in DRAFT status, persists it and returns it")
  void givenValidCoreRequestWhenCreateVacancyThenReturnsCreatedVacancyInDraft() {
    var requestBody =
        """
        {
          "jobTitle": "Sales Manager",
          "category": "SALES",
          "location": {
            "country": "NL",
            "city": "Amsterdam"
          },
          "workplaceType": "HYBRID",
          "minHoursPerWeek": 32,
          "maxHoursPerWeek": 40
        }
        """;

    var result = makeRequestAndExpectedStatus(requestBody, HttpStatus.CREATED);

    assertInstanceOf(UUID.class, result.getId());
    assertEquals(VacancyStatus.DRAFT, result.getStatus());
    assertEquals("Sales Manager", result.getJobTitle());
    assertEquals("NL", result.getLocation().getCountry());
    assertEquals("Amsterdam", result.getLocation().getCity());
    assertEquals(BigDecimal.valueOf(32), result.getMinHoursPerWeek());
    assertEquals(BigDecimal.valueOf(40), result.getMaxHoursPerWeek());
    assertNotNull(result.getCreatedAt());

    assertTrue(vacancyJpaRepository.findById(result.getId()).isPresent());
  }

  private VacancyResponse makeRequestAndExpectedStatus(String requestBody, HttpStatus status) {
    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/vacancy")
            .then()
            .statusCode(status.value())
            .extract()
            .asString();
    return JSON_MAPPER.readValue(response, VacancyResponse.class);
  }
}
