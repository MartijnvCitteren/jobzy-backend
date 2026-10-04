package app.jobzy.api.adapter.out.ai.vacancy;

import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AiVacancyDescriptionMapper {

  VacancyDescriptionPromptInfo vacancyDescriptionCommandToMap(
      GenerateVacancyDescriptionCommand command) {
    return new VacancyDescriptionPromptInfo(buildCommandMap(command));
  }

  private Map<String, String> buildCommandMap(GenerateVacancyDescriptionCommand command) {
    Map<String, String> infoMap = new HashMap<>();
    infoMap.put("tasks", command.tasks());
    infoMap.put("team", command.team());
    infoMap.put("niceAboutJob", command.niceAboutJob());
    return infoMap;
  }

  VacancyDescription aiResultToVacancyDescription(AiGeneratedVacancyResponse response) {
    return VacancyDescription.builder()
        .summary(response.summary)
        .jobDescription(response.jobDescription)
        .aboutUs(response.aboutUs)
        .tasks(response.tasks)
        .source(response.source)
        .build();
  }
}
