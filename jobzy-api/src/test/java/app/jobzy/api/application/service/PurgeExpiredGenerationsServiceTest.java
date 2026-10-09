package app.jobzy.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.shared.config.GenerationProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurgeExpiredGenerationsServiceTest {

  @Mock private VacancyDescriptionGenerationRepository generationRepository;

  @Test
  @DisplayName(
      "given a retention period, when purge then generations requested before now minus that"
          + " period are deleted")
  void givenRetentionPeriodWhenPurgeThenDeletesBeforeNowMinusRetention() {
    var now = LocalDateTime.of(2026, 3, 2, 12, 0);
    var clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    var service =
        new PurgeExpiredGenerationsService(
            generationRepository, clock, new GenerationProperties(Duration.ofHours(32)));
    when(generationRepository.deleteRequestedBefore(now.minusHours(32))).thenReturn(3);

    var deleted = service.purgeExpired();

    assertEquals(3, deleted);
  }
}
