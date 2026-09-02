package app.jobzy.api.domain.vacancy;

import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyId;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.math.BigDecimal;
import java.time.Instant;

public class Vacancy {

  private final VacancyId id;
  private final String jobTitle;
  private final VacancyCategory category;
  private final Location location;
  private final WorkplaceType workplaceType;
  private final BigDecimal hoursPerWeek;
  private final Instant createdAt;
  private VacancyStatus status;

  private Vacancy(
      VacancyId id,
      String jobTitle,
      VacancyCategory category,
      Location location,
      WorkplaceType workplaceType,
      BigDecimal hoursPerWeek,
      VacancyStatus status,
      Instant createdAt) {
    if (jobTitle == null || jobTitle.isBlank()) {
      throw new IllegalArgumentException("Vacancy requires a job title");
    }
    if (category == null) {
      throw new IllegalArgumentException("Vacancy requires a category");
    }
    if (location == null) {
      throw new IllegalArgumentException("Vacancy requires a location");
    }
    if (workplaceType == null) {
      throw new IllegalArgumentException("Vacancy requires a workplace type");
    }
    if (hoursPerWeek == null || hoursPerWeek.signum() <= 0) {
      throw new IllegalArgumentException("Vacancy requires a positive number of hours per week");
    }
    this.id = id;
    this.jobTitle = jobTitle;
    this.category = category;
    this.location = location;
    this.workplaceType = workplaceType;
    this.hoursPerWeek = hoursPerWeek;
    this.status = status;
    this.createdAt = createdAt;
  }

  public static Vacancy createCore(
      String jobTitle,
      VacancyCategory category,
      Location location,
      WorkplaceType workplaceType,
      BigDecimal hoursPerWeek) {
    return new Vacancy(
        VacancyId.newId(),
        jobTitle,
        category,
        location,
        workplaceType,
        hoursPerWeek,
        VacancyStatus.DRAFT,
        Instant.now());
  }

  public VacancyId getId() {
    return id;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public VacancyCategory getCategory() {
    return category;
  }

  public Location getLocation() {
    return location;
  }

  public WorkplaceType getWorkplaceType() {
    return workplaceType;
  }

  public BigDecimal getHoursPerWeek() {
    return hoursPerWeek;
  }

  public VacancyStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
