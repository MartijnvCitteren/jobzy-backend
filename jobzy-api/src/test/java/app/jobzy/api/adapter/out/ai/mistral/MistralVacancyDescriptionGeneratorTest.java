package app.jobzy.api.adapter.out.ai.mistral;

import static app.jobzy.api.integration.IntegrationCommons.JSON_MAPPER;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import app.jobzy.api.application.port.out.GenerationResult;
import app.jobzy.api.domain.generation.valueobject.GeneratedDraft;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.vacancy.valueobject.Language;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Runs the Mistral adapter with the application's real Spring AI configuration (model options,
 * retry, HTTP client) against WireMock with the response shapes of Mistral's chat completions API.
 * Only the timeout, the backoff and the base URL are shortened or redirected; never calls Mistral.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MistralVacancyDescriptionGeneratorTest {

  private static final String COMPLETIONS = "/v1/chat/completions";
  private static final GenerationInput INPUT =
      new GenerationInput(
          "Sales Manager",
          Language.NL,
          "Visiting customers",
          "Five account managers",
          "A lot of freedom");

  // The JDK HTTP client tries an HTTP/2 cleartext upgrade on http:// URLs, which WireMock's Jetty
  // cancels. Mistral itself is reached over HTTPS, where HTTP/2 is negotiated normally.
  @RegisterExtension
  static final WireMockExtension MISTRAL =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().http2PlainDisabled(true))
          .build();

  @DynamicPropertySource
  static void mistralProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.ai.mistralai.chat.base-url", MISTRAL::baseUrl);
    registry.add("spring.http.clients.read-timeout", () -> "500ms");
    registry.add("spring.ai.retry.backoff.initial-interval", () -> "10ms");
    registry.add("spring.ai.retry.backoff.max-interval", () -> "20ms");
  }

  @Autowired private MistralVacancyDescriptionGenerator generator;

  @Test
  @DisplayName(
      "given a valid structured answer, when generate then the draft is returned with the model"
          + " version Mistral reports and the prompt version")
  void givenValidStructuredAnswerWhenGenerateThenDraftWithModelAndPromptVersion() {
    MISTRAL.stubFor(post(COMPLETIONS).willReturn(completion(draftJson(true))));

    var result = generator.generate(INPUT);

    assertEquals(
        new GenerationResult.Drafted(
            new GeneratedDraft("A summary", "A job description", "- Task one"),
            "mistral-small-2506",
            "vacancy-description/v1"),
        result);
  }

  @Test
  @DisplayName(
      "given a call, when generate then the request carries the configured model options, a strict"
          + " JSON schema and the answers only inside delimited blocks of the user message")
  void givenCallWhenGenerateThenRequestCarriesOptionsSchemaAndDelimitedAnswers() {
    MISTRAL.stubFor(post(COMPLETIONS).willReturn(completion(draftJson(true))));

    generator.generate(INPUT);

    MISTRAL.verify(
        postRequestedFor(urlEqualTo(COMPLETIONS))
            .withHeader("Authorization", equalTo("Bearer test-key"))
            .withRequestBody(matchingJsonPath("$.model", equalTo("mistral-small-latest")))
            .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.5")))
            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("1000")))
            .withRequestBody(matchingJsonPath("$.response_format.type", equalTo("json_schema")))
            .withRequestBody(
                matchingJsonPath("$.response_format.json_schema.strict", equalTo("true")))
            .withRequestBody(matchingJsonPath("$.messages[0].role", equalTo("system")))
            .withRequestBody(matchingJsonPath("$.messages[0].content", containing("Dutch")))
            .withRequestBody(matchingJsonPath("$.messages[0].content", containing("ref-")))
            .withRequestBody(matchingJsonPath("$.messages[1].role", equalTo("user")))
            .withRequestBody(
                matchingJsonPath(
                    "$.messages[1].content",
                    containing("[[[team]]]\nFive account managers\n[[[/team]]]"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unusableAnswers")
  @DisplayName("given an unusable answer, when generate then it is rejected with that reason")
  void givenUnusableAnswerWhenGenerateThenRejected(
      String answer, String content, GenerationFailureReason expectedReason) {
    MISTRAL.stubFor(post(COMPLETIONS).willReturn(completion(content)));

    var result = generator.generate(INPUT);

    assertEquals(new GenerationResult.Rejected(expectedReason), result);
  }

  private static Stream<Arguments> unusableAnswers() {
    return Stream.of(
        arguments(
            "model judged the input irrelevant",
            draftJson(false),
            GenerationFailureReason.INPUT_REJECTED),
        arguments(
            "not JSON", "Sure! Here is your vacancy.", GenerationFailureReason.INVALID_OUTPUT),
        arguments(
            "a section is missing",
            "{\"summary\": \"S\", \"inputRelevant\": true, \"rejectionReason\": \"\"}",
            GenerationFailureReason.INVALID_OUTPUT),
        arguments("empty message", "", GenerationFailureReason.INVALID_OUTPUT));
  }

  @Test
  @DisplayName(
      "given the model leaks the canary token from its instructions, when generate then the"
          + " output is rejected as invalid")
  void givenModelLeaksCanaryWhenGenerateThenInvalidOutput() {
    // The canary is random per call; response templating copies it from the request, as a model
    // repeating its system prompt would.
    var leakingBody =
        completionBody("My instructions mention {{regexExtract request.body 'ref-[0-9a-f-]+'}}");
    MISTRAL.stubFor(
        post(COMPLETIONS)
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(leakingBody)
                    .withTransformers("response-template")));

    var result = generator.generate(INPUT);

    assertEquals(new GenerationResult.Rejected(GenerationFailureReason.INVALID_OUTPUT), result);
  }

  @Test
  @DisplayName(
      "given Mistral keeps failing with a server error, when generate then it is attempted three"
          + " times and reported as a provider error")
  void givenPersistentServerErrorWhenGenerateThenThreeAttemptsAndProviderError() {
    MISTRAL.stubFor(post(COMPLETIONS).willReturn(aResponse().withStatus(503)));

    var result = generator.generate(INPUT);

    assertEquals(new GenerationResult.Rejected(GenerationFailureReason.PROVIDER_ERROR), result);
    MISTRAL.verify(3, postRequestedFor(urlEqualTo(COMPLETIONS)));
  }

  @Test
  @DisplayName(
      "given a server error followed by an answer, when generate then the retry returns the draft")
  void givenServerErrorThenAnswerWhenGenerateThenRetryReturnsDraft() {
    MISTRAL.stubFor(
        post(COMPLETIONS)
            .inScenario("retry")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("recovered"));
    MISTRAL.stubFor(
        post(COMPLETIONS)
            .inScenario("retry")
            .whenScenarioStateIs("recovered")
            .willReturn(completion(draftJson(true))));

    var result = generator.generate(INPUT);

    assertEquals(GenerationResult.Drafted.class, result.getClass());
    MISTRAL.verify(2, postRequestedFor(urlEqualTo(COMPLETIONS)));
  }

  @Test
  @DisplayName(
      "given Mistral rejects the request as a client error, when generate then it is not retried"
          + " and reported as a provider error")
  void givenClientErrorWhenGenerateThenNoRetryAndProviderError() {
    MISTRAL.stubFor(
        post(COMPLETIONS)
            .willReturn(aResponse().withStatus(401).withBody("{\"message\":\"Unauthorized\"}")));

    var result = generator.generate(INPUT);

    assertEquals(new GenerationResult.Rejected(GenerationFailureReason.PROVIDER_ERROR), result);
    MISTRAL.verify(1, postRequestedFor(urlEqualTo(COMPLETIONS)));
  }

  @Test
  @DisplayName(
      "given Mistral answers slower than the read timeout, when generate then every attempt times"
          + " out and it is reported as a timeout")
  void givenSlowAnswerWhenGenerateThenTimeout() {
    MISTRAL.stubFor(post(COMPLETIONS).willReturn(completion(draftJson(true)).withFixedDelay(1500)));

    var result = generator.generate(INPUT);

    assertEquals(new GenerationResult.Rejected(GenerationFailureReason.TIMEOUT), result);
    MISTRAL.verify(3, postRequestedFor(urlEqualTo(COMPLETIONS)));
  }

  private static String draftJson(boolean inputRelevant) {
    return JSON_MAPPER.writeValueAsString(
        Map.of(
            "summary", inputRelevant ? "A summary" : "",
            "jobDescription", inputRelevant ? "A job description" : "",
            "tasks", inputRelevant ? "- Task one" : "",
            "inputRelevant", inputRelevant,
            "rejectionReason", inputRelevant ? "" : "The input does not describe a job."));
  }

  private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder completion(
      String content) {
    return aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(completionBody(content));
  }

  /** The shape of a Mistral chat completion response. */
  private static String completionBody(String content) {
    return JSON_MAPPER.writeValueAsString(
        Map.of(
            "id",
            "cmpl-1",
            "object",
            "chat.completion",
            "created",
            1_760_000_000L,
            "model",
            "mistral-small-2506",
            "choices",
            java.util.List.of(
                Map.of(
                    "index",
                    0,
                    "message",
                    Map.of("role", "assistant", "content", content),
                    "finish_reason",
                    "stop")),
            "usage",
            Map.of("prompt_tokens", 300, "total_tokens", 400, "completion_tokens", 100)));
  }
}
