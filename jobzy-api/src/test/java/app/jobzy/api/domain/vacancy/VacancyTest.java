package app.jobzy.api.domain.vacancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.jobzy.api.domain.vacancy.valueobject.HoursPerWeek;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VacancyTest {

  @Test
  @DisplayName("given builder with no status, when build then status defaults to draft")
  void givenBuilderWithNoStatusWhenBuildThenStatusDefaultsToDraft() {
    var vacancy =
        Vacancy.builder()
            .jobTitle("Backend Engineer")
            .category(VacancyCategory.ENGINEERING)
            .location(new Location("The Netherlands", "Leiden"))
            .workplaceType(WorkplaceType.REMOTE)
            .hoursPerWeek(new HoursPerWeek(BigDecimal.valueOf(32), BigDecimal.valueOf(40)))
            .build();

    assertEquals(VacancyStatus.DRAFT, vacancy.getStatus());
  }

  @Test
  @DisplayName("given builder with all fields, when build then vacancy exposes those fields")
  void givenBuilderWithAllFieldsWhenBuildThenVacancyExposesThoseFields() {
    var location = new Location("The Netherlands", "Amsterdam");
    var hoursPerWeek = new HoursPerWeek(BigDecimal.valueOf(24), BigDecimal.valueOf(36));

    var vacancy =
        Vacancy.builder()
            .jobTitle("Sales Manager")
            .category(VacancyCategory.SALES)
            .location(location)
            .workplaceType(WorkplaceType.HYBRID)
            .hoursPerWeek(hoursPerWeek)
            .status(VacancyStatus.PUBLISHED)
            .build();

    assertEquals("Sales Manager", vacancy.getJobTitle());
    assertEquals(VacancyCategory.SALES, vacancy.getCategory());
    assertEquals(location, vacancy.getLocation());
    assertEquals(WorkplaceType.HYBRID, vacancy.getWorkplaceType());
    assertEquals(hoursPerWeek, vacancy.getHoursPerWeek());
    assertEquals(VacancyStatus.PUBLISHED, vacancy.getStatus());
  }

  @Test
  @DisplayName("given vacancy, when setters are used then getters reflect the new values")
  void givenVacancyWhenSettersAreUsedThenGettersReflectTheNewValues() {
    var vacancy = Vacancy.builder().build();
    var location = new Location("Belgium", "Antwerp");
    var hoursPerWeek = new HoursPerWeek(BigDecimal.valueOf(20), BigDecimal.valueOf(30));

    vacancy.setJobTitle("Recruiter");
    vacancy.setCategory(VacancyCategory.HUMAN_RESOURCES);
    vacancy.setLocation(location);
    vacancy.setWorkplaceType(WorkplaceType.ONSITE);
    vacancy.setHoursPerWeek(hoursPerWeek);
    vacancy.setStatus(VacancyStatus.CLOSED);

    assertEquals("Recruiter", vacancy.getJobTitle());
    assertEquals(VacancyCategory.HUMAN_RESOURCES, vacancy.getCategory());
    assertEquals(location, vacancy.getLocation());
    assertEquals(WorkplaceType.ONSITE, vacancy.getWorkplaceType());
    assertEquals(hoursPerWeek, vacancy.getHoursPerWeek());
    assertEquals(VacancyStatus.CLOSED, vacancy.getStatus());
  }

  @Test
  @DisplayName("given two vacancies with the same id, when equals then returns true")
  void givenTwoVacanciesWithSameIdWhenEqualsThenReturnsTrue() {
    var vacancy = Vacancy.builder().build();

    assertTrue(vacancy.equals(vacancy));
  }

  @Test
  @DisplayName("given two vacancies with different ids, when equals then returns false")
  void givenTwoVacanciesWithDifferentIdsWhenEqualsThenReturnsFalse() {
    var vacancy1 = Vacancy.builder().build();
    var vacancy2 = Vacancy.builder().build();

    assertNotEquals(vacancy1, vacancy2);
  }

  @Test
  @DisplayName("given vacancy and non-vacancy object, when equals then returns false")
  void givenVacancyAndNonVacancyObjectWhenEqualsThenReturnsFalse() {
    var vacancy = Vacancy.builder().build();

    assertFalse(vacancy.equals("not a vacancy"));
  }

  @Test
  @DisplayName("given vacancy, when hashCode then matches hash of id")
  void givenVacancyWhenHashCodeThenMatchesHashOfId() {
    var vacancy = Vacancy.builder().build();

    assertEquals(vacancy.getId().hashCode(), vacancy.hashCode());
  }

  @Test
  @DisplayName("given vacancy, when toString then contains job title")
  void givenVacancyWhenToStringThenContainsJobTitle() {
    var vacancy = Vacancy.builder().jobTitle("Backend Engineer").build();

    assertTrue(vacancy.toString().contains("Backend Engineer"));
  }
}
