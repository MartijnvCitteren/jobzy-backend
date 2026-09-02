package app.jobzy.api.vacancy.adapter.in.web.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancyResponseMapperTest {

  @InjectMocks
  private VacancyResponseMapperImpl mapper;

  @Test
  @DisplayName("given vacancy, when toVacancyResponse then maps all fields")
  void givenVacancyWhenToVacancyResponseThenMapsAllFields() {
    Vacancy vacancy =
        Vacancy.createCore(
            "Backend Engineer",
            VacancyCategory.ENGINEERING,
            new Location("NL", "Leiden"),
            WorkplaceType.HYBRID,
            BigDecimal.valueOf(32));

    VacancyResponse result = mapper.toVacancyResponse(vacancy);

    assertEquals(vacancy.getId().value(), result.getId());
    assertEquals(VacancyStatus.DRAFT.name(), result.getStatus().getValue());
    assertEquals("Backend Engineer", result.getJobTitle());
    assertEquals(VacancyCategory.ENGINEERING.name(), result.getCategory().getValue());
    assertEquals("NL", result.getLocation().getCountry());
    assertEquals("Leiden", result.getLocation().getCity());
    assertEquals(WorkplaceType.HYBRID.name(), result.getWorkplaceType().getValue());
    assertEquals(BigDecimal.valueOf(32), result.getHoursPerWeek());
    assertEquals(vacancy.getCreatedAt().atOffset(ZoneOffset.UTC), result.getCreatedAt());
  }
}
