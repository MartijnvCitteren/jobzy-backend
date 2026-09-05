package app.jobzy.api.adapter.in.rest.vacancy.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancyDescriptionResponseMapperTest {

  @InjectMocks private VacancyDescriptionResponseMapperImpl mapper;

  @Test
  @DisplayName("given a vacancy description, when toResponse then maps every field except source")
  void givenVacancyDescriptionWhenToResponseThenMapsEveryFieldExceptSource() {
    var description =
        new VacancyDescription(
            "Summary",
            "Job description",
            "Tasks",
            "What we offer",
            "About us",
            VacancyDescriptionSource.MANUAL);

    var result = mapper.toResponse(description);

    assertEquals("Summary", result.getSummary());
    assertEquals("Job description", result.getJobDescription());
    assertEquals("Tasks", result.getTasks());
    assertEquals("What we offer", result.getWhatWeOffer());
    assertEquals("About us", result.getAboutUs());
  }
}
