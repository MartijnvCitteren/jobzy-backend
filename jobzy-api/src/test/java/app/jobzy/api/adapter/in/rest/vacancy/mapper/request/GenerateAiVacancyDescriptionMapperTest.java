package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.vacancy.adapter.in.web.contract.GenerateVacancyDescriptionRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateAiVacancyDescriptionMapperTest {

  @InjectMocks private GenerateVacancyDescriptionMapperImpl mapper;

  @Test
  @DisplayName(
      "given a vacancy id and generate description request, when toCommand then maps all fields")
  void givenVacancyIdAndGenerateDescriptionRequestWhenToCommandThenMapsAllFields() {
    var id = UUID.randomUUID();
    var request =
        new GenerateVacancyDescriptionRequest(
            "Most important tasks", "Team description", "Why nice job");

    var result = mapper.toCommand(id, request);

    assertEquals(id, result.vacancyId());
    assertEquals(request.getMostImportantTasks(), result.tasks());
    assertEquals(request.getTeam(), result.team());
    assertEquals(request.getWhyNiceJob(), result.niceAboutJob());
  }
}
