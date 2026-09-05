package app.jobzy.api.domain.vacancy;

import app.jobzy.api.domain.BaseObject;
import app.jobzy.api.domain.UuidV7Generator;
import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.util.Objects;
import java.util.UUID;

public class Vacancy extends BaseObject {

  private final UUID id;
  private String jobTitle;
  private VacancyCategory category;
  private Location location;
  private WorkplaceType workplaceType;
  private HoursPerWeek hoursPerWeek;
  private VacancyStatus status;

  private Vacancy(
      UUID id,
      String jobTitle,
      VacancyCategory category,
      Location location,
      WorkplaceType workplaceType,
      HoursPerWeek hoursPerWeek,
      VacancyStatus status) {
    this.id = id;
    this.jobTitle = jobTitle;
    this.category = category;
    this.location = location;
    this.workplaceType = workplaceType;
    this.hoursPerWeek = hoursPerWeek;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public VacancyCategory getCategory() {
    return category;
  }

  public void setCategory(VacancyCategory category) {
    this.category = category;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public WorkplaceType getWorkplaceType() {
    return workplaceType;
  }

  public void setWorkplaceType(WorkplaceType workplaceType) {
    this.workplaceType = workplaceType;
  }

  public HoursPerWeek getHoursPerWeek() {
    return hoursPerWeek;
  }

  public void setHoursPerWeek(HoursPerWeek hoursPerWeek) {
    this.hoursPerWeek = hoursPerWeek;
  }

  public VacancyStatus getStatus() {
    return status;
  }

  public void setStatus(VacancyStatus status) {
    this.status = status;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vacancy vacancy)) {
      return false;
    }
    return Objects.equals(getId(), vacancy.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return "Vacancy{"
        + "id="
        + id
        + ", jobTitle='"
        + jobTitle
        + '\''
        + ", category="
        + category
        + ", location="
        + location
        + ", workplaceType="
        + workplaceType
        + ", hoursPerWeek="
        + hoursPerWeek
        + ", status="
        + status
        + '}';
  }

  public static class Builder {
    private final UUID id = UuidV7Generator.getUUID();
    private String jobTitle;
    private VacancyCategory category;
    private Location location;
    private WorkplaceType workplaceType;
    private HoursPerWeek hoursPerWeek;
    private VacancyStatus status = VacancyStatus.DRAFT;

    private Builder() {}

    public Builder jobTitle(String jobTitle) {
      this.jobTitle = jobTitle;
      return this;
    }

    public Builder category(VacancyCategory category) {
      this.category = category;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder workplaceType(WorkplaceType workplaceType) {
      this.workplaceType = workplaceType;
      return this;
    }

    public Builder hoursPerWeek(HoursPerWeek hoursPerWeek) {
      this.hoursPerWeek = hoursPerWeek;
      return this;
    }

    public Builder status(VacancyStatus status) {
      this.status = status;
      return this;
    }

    public Vacancy build() {
      return new Vacancy(id, jobTitle, category, location, workplaceType, hoursPerWeek, status);
    }
  }
}
