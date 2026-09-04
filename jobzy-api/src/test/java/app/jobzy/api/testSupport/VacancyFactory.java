package app.jobzy.api.testSupport;

import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.Vacancy.Builder;
import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.math.BigDecimal;

public class VacancyFactory {
  public static final String JOB_TITLE = "Sales Manager";
  public static final VacancyCategory CATEGORY = VacancyCategory.SALES;
  public static final BigDecimal MIN_HOURS_PER_WEEK = BigDecimal.valueOf(32);
  public static final BigDecimal MAX_HOURS_PER_WEEK = BigDecimal.valueOf(40);
  public static final String COUNTRY = "The Netherlands";
  public static final String CITY = "Amsterdam";
  public static final VacancyStatus STATUS = VacancyStatus.DRAFT;
  public static final WorkplaceType WORKPLACE_TYPE = WorkplaceType.HYBRID;

  public static Builder getFilledCoreVacancy() {
    return Vacancy.builder()
        .jobTitle(JOB_TITLE)
        .category(CATEGORY)
        .hoursPerWeek(new HoursPerWeek(MIN_HOURS_PER_WEEK, MAX_HOURS_PER_WEEK))
        .location(new Location(COUNTRY, CITY))
        .status(STATUS)
        .workplaceType(WORKPLACE_TYPE);
  }
}
