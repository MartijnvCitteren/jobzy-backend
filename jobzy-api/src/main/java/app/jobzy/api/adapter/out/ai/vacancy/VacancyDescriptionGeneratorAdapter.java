package app.jobzy.api.adapter.out.ai.vacancy;

import app.jobzy.api.adapter.out.ai.support.mistral.MistralAi;
import app.jobzy.api.adapter.out.ai.support.prompt.PromptFactory;
import app.jobzy.api.adapter.out.exception.InvalidAiResponse;
import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.application.port.out.AiVacancyDescription;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public class VacancyDescriptionGeneratorAdapter implements AiVacancyDescription {
  private final AiVacancyDescriptionMapper aiVacancyDescriptionMapper;
  private final MistralAi mistralAi;
  private final JsonMapper jsonMapper;

  @Override
  public VacancyDescription generateVacancyDescription(GenerateVacancyDescriptionCommand command) {
    var generationInfo = aiVacancyDescriptionMapper.vacancyDescriptionCommandToMap(command);
    var prompt = PromptFactory.createPrompt(generationInfo);
    var chatResponse = mistralAi.generateText(prompt, AiGeneratedVacancyResponse.class);
    var aiVacancyDescription = validateAndExtractResponse(chatResponse);
    return aiVacancyDescriptionMapper.aiResultToVacancyDescription(aiVacancyDescription);
  }

  private AiGeneratedVacancyResponse validateAndExtractResponse(ChatResponse response) {
    validate(response);
    return extractResponse(response);
  }

  private void validate(ChatResponse response) {
    if (response == null
        || response.getResult() == null
        || response.getResult().getOutput().getText() == null) {
      throw new InvalidAiResponse("Mistral returned an empty chat response");
    }
  }

  private AiGeneratedVacancyResponse extractResponse(ChatResponse response) {
    var jsonString = response.getResult().getOutput().getText();
    return jsonMapper.convertValue(jsonString, AiGeneratedVacancyResponse.class);
  }
}
