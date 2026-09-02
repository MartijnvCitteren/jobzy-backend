package app.jobzy.api.adapter.in.rest.vacancy.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.jobzy.api.application.port.in.dto.LocationDto;
import app.jobzy.api.application.port.in.dto.VacancyCategoryDto;
import app.jobzy.api.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.application.port.in.dto.WorkplaceTypeDto;
import app.jobzy.api.vacancy.adapter.in.web.contract.Location;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCategory;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCoreRequest;
import app.jobzy.api.vacancy.adapter.in.web.contract.WorkplaceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = VacancyCoreRequestMapperTest.MapperTestConfig.class)
class VacancyCoreRequestMapperTest {

  @Configuration
  @ComponentScan(basePackageClasses = VacancyCoreRequestMapper.class)
  static class MapperTestConfig {}

  @Autowired private VacancyCoreRequestMapper mapper;

  @Test
  @DisplayName(
      "given vacancy core request, when toDto then maps all fields, delegating to sub-mappers")
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
