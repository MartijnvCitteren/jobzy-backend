package app.jobzy.api.vacancy.adapter.in.web.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import app.jobzy.api.vacancy.domain.Location;
import app.jobzy.api.vacancy.domain.Vacancy;
import app.jobzy.api.vacancy.domain.VacancyCategory;
import app.jobzy.api.vacancy.domain.VacancyStatus;
import app.jobzy.api.vacancy.domain.WorkplaceType;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = VacancyResponseMapperTest.MapperTestConfig.class)
class VacancyResponseMapperTest {

  @Configuration
  @ComponentScan(basePackageClasses = VacancyResponseMapper.class)
  static class MapperTestConfig {}

  @Autowired private VacancyResponseMapper mapper;

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
