package app.jobzy.api.integration.vacancy;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Datasource/JPA autoconfiguration is excluded and {@code VacancyRepository} is mocked: {@code
 * createVacancy} does not persist yet (see {@code CreateVacancyService}), and the project has no
 * test database (only a real SQL Server via docker-compose) to back the JPA adapter. Revisit once
 * vacancy creation is wired up to persist.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class,
      DataJpaRepositoriesAutoConfiguration.class
    })
class VacancyControllerIntegrationTest {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @LocalServerPort private int port;

  @MockitoBean private VacancyRepository vacancyRepository;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.basePath = "/api/v1";
  }

  @Test
  @DisplayName("POST /vacancy creates a vacancy in DRAFT status and returns it")
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

    var responseBody =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/vacancy")
            .then()
            .statusCode(201)
            .extract()
            .asString();

    var vacancyResponse = JSON_MAPPER.readValue(responseBody, VacancyResponse.class);

    assertNotNull(vacancyResponse.getId());
    assertEquals(VacancyStatus.DRAFT, vacancyResponse.getStatus());
    assertEquals("Sales Manager", vacancyResponse.getJobTitle());
    assertEquals("NL", vacancyResponse.getLocation().getCountry());
    assertEquals("Amsterdam", vacancyResponse.getLocation().getCity());
    assertEquals(BigDecimal.valueOf(32), vacancyResponse.getMinHoursPerWeek());
    assertEquals(BigDecimal.valueOf(40), vacancyResponse.getMaxHoursPerWeek());
    assertNotNull(vacancyResponse.getCreatedAt());
  }
}
