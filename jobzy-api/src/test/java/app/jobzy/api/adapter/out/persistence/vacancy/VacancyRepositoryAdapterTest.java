package app.jobzy.api.adapter.out.persistence.vacancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.jobzy.api.adapter.out.persistence.vacancy.mapper.VacancyDescriptionJpaMapperImpl;
import app.jobzy.api.adapter.out.persistence.vacancy.mapper.VacancyJpaMapperImpl;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;
import app.jobzy.api.testSupport.VacancyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({
  VacancyRepositoryAdapter.class,
  VacancyJpaMapperImpl.class,
  VacancyDescriptionJpaMapperImpl.class
})
class VacancyRepositoryAdapterTest {

  @Autowired private VacancyRepositoryAdapter vacancyRepositoryAdapter;
  @Autowired private VacancyDescriptionJpaRepository descriptionJpaRepository;

  @Test
  @DisplayName(
      "given a vacancy with a full description, when saved and reloaded then every field"
          + " round-trips")
  void givenVacancyWithFullDescriptionWhenSavedAndReloadedThenEveryFieldRoundTrips() {
    var description =
        new VacancyDescription(
            "Summary",
            "Job description",
            "Tasks",
            "What we offer",
            "About us",
            VacancyDescriptionSource.MANUAL);
    var vacancy = VacancyFactory.getFilledCoreVacancy().description(description).build();

    vacancyRepositoryAdapter.save(vacancy);
    var reloaded = vacancyRepositoryAdapter.findById(vacancy.getId()).orElseThrow();

    assertEquals(description, reloaded.getDescription());
    assertEquals(1, descriptionJpaRepository.findByVacancyId(vacancy.getId()).stream().count());
  }

  @Test
  @DisplayName(
      "given a vacancy with an existing description, when saved again with a different"
          + " description then the new content replaces the old and no duplicate row is created")
  void givenVacancyWithExistingDescriptionWhenSavedAgainThenReplacesWithoutDuplicateRow() {
    var firstDescription =
        new VacancyDescription(
            "Old summary", null, null, null, null, VacancyDescriptionSource.MANUAL);
    var vacancy = VacancyFactory.getFilledCoreVacancy().description(firstDescription).build();
    vacancyRepositoryAdapter.save(vacancy);

    var secondDescription =
        new VacancyDescription(
            "New summary",
            "New job description",
            null,
            null,
            null,
            VacancyDescriptionSource.MANUAL);
    vacancy.setDescription(secondDescription);
    vacancyRepositoryAdapter.save(vacancy);

    var reloaded = vacancyRepositoryAdapter.findById(vacancy.getId()).orElseThrow();
    assertEquals(secondDescription, reloaded.getDescription());
    assertEquals(1, descriptionJpaRepository.count());
  }

  @Test
  @DisplayName(
      "given a vacancy with no description, when saved and reloaded then description is null and"
          + " no description row exists")
  void givenVacancyWithNoDescriptionWhenSavedAndReloadedThenDescriptionIsNullAndNoRowExists() {
    var vacancy = VacancyFactory.getFilledCoreVacancy().build();

    vacancyRepositoryAdapter.save(vacancy);
    var reloaded = vacancyRepositoryAdapter.findById(vacancy.getId()).orElseThrow();

    assertNull(reloaded.getDescription());
    assertTrue(descriptionJpaRepository.findByVacancyId(vacancy.getId()).isEmpty());
  }
}
