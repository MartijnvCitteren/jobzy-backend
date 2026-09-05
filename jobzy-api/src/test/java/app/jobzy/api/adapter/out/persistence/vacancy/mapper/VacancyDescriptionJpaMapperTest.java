package app.jobzy.api.adapter.out.persistence.vacancy.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.jobzy.api.adapter.out.persistence.vacancy.VacancyDescriptionJpaEntity;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancyDescriptionJpaMapperTest {

  @InjectMocks private VacancyDescriptionJpaMapperImpl mapper;

  @Test
  @DisplayName("given entity, when toDomain then maps all fields")
  void givenEntityWhenToDomainThenMapsAllFields() {
    var entity = new VacancyDescriptionJpaEntity();
    entity.setSummary("Summary");
    entity.setJobDescription("Job description");
    entity.setTasks("Tasks");
    entity.setWhatWeOffer("What we offer");
    entity.setAboutUs("About us");
    entity.setSource(VacancyDescriptionSource.MANUAL);

    VacancyDescription result = mapper.toDomain(entity);

    assertEquals("Summary", result.summary());
    assertEquals("Job description", result.jobDescription());
    assertEquals("Tasks", result.tasks());
    assertEquals("What we offer", result.whatWeOffer());
    assertEquals("About us", result.aboutUs());
    assertEquals(VacancyDescriptionSource.MANUAL, result.source());
  }

  @Test
  @DisplayName("given no existing entity, when toJpaEntity then generates a fresh id and createdAt")
  void givenNoExistingEntityWhenToJpaEntityThenGeneratesFreshIdAndCreatedAt() {
    var vacancyId = UUID.randomUUID();
    var description =
        new VacancyDescription(
            "Summary",
            "Job description",
            "Tasks",
            "What we offer",
            "About us",
            VacancyDescriptionSource.MANUAL);

    var result = mapper.toJpaEntity(vacancyId, description, null);

    assertNotNull(result.getId());
    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    assertEquals(vacancyId, result.getVacancyId());
    assertEquals("Summary", result.getSummary());
    assertEquals(VacancyDescriptionSource.MANUAL, result.getSource());
  }

  @Test
  @DisplayName("given an existing entity, when toJpaEntity then reuses its id and createdAt")
  void givenExistingEntityWhenToJpaEntityThenReusesIdAndCreatedAt() {
    var vacancyId = UUID.randomUUID();
    var existingId = UUID.randomUUID();
    var existingCreatedAt = LocalDateTime.now().minusDays(1);
    var existing = new VacancyDescriptionJpaEntity();
    existing.setId(existingId);
    existing.setVacancyId(vacancyId);
    existing.setCreatedAt(existingCreatedAt);
    var newDescription =
        new VacancyDescription(
            "New summary", null, null, null, null, VacancyDescriptionSource.MANUAL);

    var result = mapper.toJpaEntity(vacancyId, newDescription, existing);

    assertEquals(existingId, result.getId());
    assertEquals(existingCreatedAt, result.getCreatedAt());
    assertEquals("New summary", result.getSummary());
    assertNull(result.getJobDescription());
  }
}
