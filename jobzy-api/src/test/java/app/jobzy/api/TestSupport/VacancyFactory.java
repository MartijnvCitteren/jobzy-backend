package app.jobzy.api.TestSupport;

import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.Vacancy.Builder;
import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.math.BigDecimal;

public class VacancyFactory {

  public static Builder getFilledCoreVacancy() {
    return Vacancy.builder()
        .jobTitle("Sales Manager")
        .category(VacancyCategory.SALES)
        .hoursPerWeek(new HoursPerWeek(BigDecimal.valueOf(15.5), BigDecimal.valueOf(36)))
        .location(new Location("The Netherlands", "Amsterdam"))
        .status(VacancyStatus.DRAFT)
        .workplaceType(WorkplaceType.HYBRID);
  }
}
