package app.jobzy.api.integration;

import app.jobzy.api.application.port.out.VacancyDescriptionGenerator;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Base class for RestAssured integration tests: boots the application on a random port against the
 * in-memory H2 database of the {@code test} profile and points RestAssured at it.
 *
 * <p>{@code @Async} work runs synchronously ({@link SynchronousAsyncConfig}) and the AI generator
 * port is a mock, so no test ever calls an AI provider. Declared here rather than per test class so
 * every integration test shares one application context.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SynchronousAsyncConfig.class)
public abstract class BaseIntegrationTest {

  @LocalServerPort protected int port;

  @MockitoBean protected VacancyDescriptionGenerator generator;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @BeforeEach
  void configureRestAssured() {
    RestAssured.port = port;
    RestAssured.basePath = contextPath;
  }

  @AfterEach
  void resetRestAssured() {
    RestAssured.reset();
  }
}
