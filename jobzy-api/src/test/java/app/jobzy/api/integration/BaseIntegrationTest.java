package app.jobzy.api.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for RestAssured integration tests: boots the application on a random port against the
 * in-memory H2 database of the {@code test} profile and points RestAssured at it.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @LocalServerPort protected int port;

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
