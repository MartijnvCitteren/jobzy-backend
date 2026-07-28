package app.jobzy.api.vacancy.adapter.in.web.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.vacancy.application.port.in.dto.LocationDto;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCategoryDto;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.vacancy.application.port.in.dto.WorkplaceTypeDto;
import app.jobzy.api.vacancy.domain.rest.Location;
import app.jobzy.api.vacancy.domain.rest.VacancyCategory;
import app.jobzy.api.vacancy.domain.rest.VacancyCoreRequest;
import app.jobzy.api.vacancy.domain.rest.WorkplaceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VacancyCoreRequestMapperTest {

  private final VacancyCoreRequestMapper mapper = new VacancyCoreRequestMapperImpl();

  @Test
  @DisplayName("given vacancy core request, when toDto then maps all fields")
  void givenVacancyCoreRequestWhenToDtoThenMapsAllFields() {
    VacancyCoreRequest request =
        new VacancyCoreRequest(
            "Backend Engineer",
            VacancyCategory.ENGINEERING,
            new Location("NL", "Leiden"),
            WorkplaceType.HYBRID,
            BigDecimal.valueOf(32));

    VacancyCoreRequestDto result = mapper.toDto(request);

    assertEquals("Backend Engineer", result.jobTitle());
    assertEquals(VacancyCategoryDto.ENGINEERING, result.category());
    assertEquals(new LocationDto("NL", "Leiden"), result.location());
    assertEquals(WorkplaceTypeDto.HYBRID, result.workplaceType());
    assertEquals(BigDecimal.valueOf(32), result.hoursPerWeek());
  }
}
