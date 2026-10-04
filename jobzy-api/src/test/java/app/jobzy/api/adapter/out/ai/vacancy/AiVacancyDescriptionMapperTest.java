package app.jobzy.api.adapter.out.ai.vacancy;

import static org.assertj.core.api.Assertions.assertThat;

import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiVacancyDescriptionMapperTest {

  private final AiVacancyDescriptionMapper mapper = new AiVacancyDescriptionMapper();

  @Test
  @DisplayName("should map command fields to prompt info map")
  void shouldMapCommandToPromptInfo() {
    var vacancyId = UUID.randomUUID();
    var command =
        GenerateVacancyDescriptionCommand.builder()
            .vacancyId(vacancyId)
            .tasks("Develop backend services")
            .team("Platform Team")
            .niceAboutJob("Work with modern technologies")
            .build();

    var result = mapper.vacancyDescriptionCommandToMap(command);

    assertThat(result).isNotNull();
    assertThat(result.getPromptValues())
        .containsEntry("tasks", "Develop backend services")
        .containsEntry("team", "Platform Team")
        .containsEntry("niceAboutJob", "Work with modern technologies")
        .hasSize(3);
  }

  @Test
  @DisplayName("should handle null values in command")
  void shouldHandleNullValues() {
    var command =
        GenerateVacancyDescriptionCommand.builder()
            .vacancyId(UUID.randomUUID())
            .tasks(null)
            .team("Platform Team")
            .niceAboutJob(null)
            .build();

    var result = mapper.vacancyDescriptionCommandToMap(command);

    assertThat(result).isNotNull();
    assertThat(result.getPromptValues())
        .containsEntry("tasks", null)
        .containsEntry("team", "Platform Team")
        .containsEntry("niceAboutJob", null);
  }
}
