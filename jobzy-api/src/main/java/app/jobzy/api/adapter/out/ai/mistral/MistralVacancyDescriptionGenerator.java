package app.jobzy.api.adapter.out.ai.mistral;

import app.jobzy.api.application.port.out.GenerationResult;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerator;
import app.jobzy.api.domain.generation.valueobject.GeneratedDraft;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Drafts a vacancy description with Mistral through Spring AI. The only class that knows the
 * provider; model, temperature, token limit, timeouts and retries are configuration (see {@code
 * application.yml}).
 *
 * <p>Defences against prompt injection, besides the input validation at the REST edge: one fixed
 * task in one call with no conversation, user text only inside delimited data blocks, the output
 * forced into a strict JSON schema in which the model can refuse ({@code inputRelevant}), and a
 * per-call canary token in the system prompt that marks the output invalid when it leaks. Length
 * and content rules on the draft are applied by the domain, for every generator alike.
 *
 * <p>Never throws for a failed generation; every failure becomes a {@link
 * GenerationResult.Rejected}. Nothing here logs the prompt, the answers or the output.
 */
@Log4j2
@Component
public class MistralVacancyDescriptionGenerator implements VacancyDescriptionGenerator {
  private final ChatClient chatClient;
  private final String configuredModel;
  private final VacancyDescriptionPrompt prompt = new VacancyDescriptionPrompt();
  private final BeanOutputConverter<MistralDraftResponse> outputConverter =
      new BeanOutputConverter<>(MistralDraftResponse.class);

  public MistralVacancyDescriptionGenerator(
      ChatClient.Builder chatClientBuilder,
      @Value("${spring.ai.mistralai.chat.model}") String configuredModel) {
    this.chatClient = chatClientBuilder.build();
    this.configuredModel = configuredModel;
  }

  @Override
  public GenerationResult generate(GenerationInput input) {
    var canary = "ref-" + UUID.randomUUID();
    ChatResponse response;
    try {
      response =
          chatClient
              .prompt()
              .options(MistralAiChatOptions.builder().outputSchema(outputConverter.getJsonSchema()))
              .system(prompt.system(input.language(), canary))
              .user(prompt.user(input))
              .call()
              .chatResponse();
    } catch (RuntimeException e) {
      return providerFailure(e);
    }
    return interpret(response, canary);
  }

  private GenerationResult interpret(ChatResponse response, String canary) {
    var result = response == null ? null : response.getResult();
    if (result == null) {
      log.warn("Mistral returned no result");
      return rejected(GenerationFailureReason.PROVIDER_ERROR);
    }
    var text = result.getOutput().getText();
    if (text == null || text.isBlank()) {
      log.warn("Mistral returned an empty message");
      return rejected(GenerationFailureReason.INVALID_OUTPUT);
    }
    if (text.contains(canary)) {
      log.warn("Mistral output contains the canary token; the instructions leaked");
      return rejected(GenerationFailureReason.INVALID_OUTPUT);
    }

    MistralDraftResponse draft;
    try {
      draft = outputConverter.convert(text);
    } catch (RuntimeException e) {
      log.warn("Mistral output does not match the response schema: {}", e.getClass().getName());
      return rejected(GenerationFailureReason.INVALID_OUTPUT);
    }
    if (!draft.inputRelevant()) {
      log.info("Mistral rejected the input as not describing a job");
      return rejected(GenerationFailureReason.INPUT_REJECTED);
    }
    if (draft.summary() == null || draft.jobDescription() == null || draft.tasks() == null) {
      log.warn("Mistral output misses a section");
      return rejected(GenerationFailureReason.INVALID_OUTPUT);
    }

    return new GenerationResult.Drafted(
        new GeneratedDraft(draft.summary(), draft.jobDescription(), draft.tasks()),
        modelOf(response),
        VacancyDescriptionPrompt.VERSION);
  }

  /** The exact model version Mistral reports (e.g. a dated snapshot), else the configured alias. */
  private String modelOf(ChatResponse response) {
    var reported = response.getMetadata().getModel();
    return reported == null || reported.isBlank() ? configuredModel : reported;
  }

  /**
   * A timeout surfaces as an I/O exception somewhere in the cause chain, after the retries are used
   * up; anything else (HTTP error status, connection refused, ...) is a provider error.
   */
  private GenerationResult providerFailure(RuntimeException e) {
    if (hasTimeoutCause(e)) {
      log.warn("Mistral did not answer in time: {}", e.toString());
      return rejected(GenerationFailureReason.TIMEOUT);
    }
    log.warn("Mistral call failed: {}", e.toString());
    return rejected(GenerationFailureReason.PROVIDER_ERROR);
  }

  private static boolean hasTimeoutCause(Throwable e) {
    for (Throwable cause = e; cause != null; cause = cause.getCause()) {
      if (cause instanceof HttpTimeoutException || cause instanceof SocketTimeoutException) {
        return true;
      }
    }
    return false;
  }

  private static GenerationResult rejected(GenerationFailureReason reason) {
    return new GenerationResult.Rejected(reason);
  }
}
