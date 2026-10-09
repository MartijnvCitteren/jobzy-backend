package app.jobzy.api.domain.generation;

import static app.jobzy.api.testSupport.GenerationFactory.MODEL;
import static app.jobzy.api.testSupport.GenerationFactory.PROMPT_VERSION;
import static app.jobzy.api.testSupport.GenerationFactory.REQUESTED_AT;
import static app.jobzy.api.testSupport.GenerationFactory.VACANCY_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.jobzy.api.domain.generation.valueobject.GeneratedDraft;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.testSupport.GenerationFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VacancyDescriptionGenerationTest {

  @Test
  @DisplayName("given vacancy and input, when start then generation is pending with a new id")
  void givenVacancyAndInputWhenStartThenPendingWithNewId() {
    var generation =
        VacancyDescriptionGeneration.start(VACANCY_ID, GenerationFactory.input(), REQUESTED_AT);

    assertNotNull(generation.getId());
    assertEquals(VACANCY_ID, generation.getVacancyId());
    assertEquals(GenerationFactory.input(), generation.getInput());
    assertEquals(REQUESTED_AT, generation.getRequestedAt());
    assertEquals(GenerationStatus.PENDING, generation.getStatus());
    assertNull(generation.getDraft());
    assertNull(generation.getFailureReason());
    assertNull(generation.getCompletedAt());
  }

  @Test
  @DisplayName("given pending generation, when a saveable draft is accepted then it is completed")
  void givenPendingGenerationWhenSaveableDraftAcceptedThenCompleted() {
    var generation = GenerationFactory.pending();
    var completedAt = REQUESTED_AT.plusSeconds(20);

    generation.acceptDraft(GenerationFactory.draft(), MODEL, PROMPT_VERSION, completedAt);

    assertEquals(GenerationStatus.COMPLETED, generation.getStatus());
    assertEquals(GenerationFactory.draft(), generation.getDraft());
    assertEquals(MODEL, generation.getModel());
    assertEquals(PROMPT_VERSION, generation.getPromptVersion());
    assertEquals(completedAt, generation.getCompletedAt());
    assertNull(generation.getFailureReason());
  }

  @Test
  @DisplayName(
      "given pending generation, when a draft that cannot be saved is accepted then it fails with"
          + " invalid output and keeps model and prompt version")
  void givenPendingGenerationWhenUnsaveableDraftAcceptedThenFailsWithInvalidOutput() {
    var generation = GenerationFactory.pending();
    var unsaveable = new GeneratedDraft("<b>Summary</b>", "Job", "Tasks");

    generation.acceptDraft(unsaveable, MODEL, PROMPT_VERSION, REQUESTED_AT.plusSeconds(20));

    assertEquals(GenerationStatus.FAILED, generation.getStatus());
    assertEquals(GenerationFailureReason.INVALID_OUTPUT, generation.getFailureReason());
    assertNull(generation.getDraft());
    assertEquals(MODEL, generation.getModel());
    assertEquals(PROMPT_VERSION, generation.getPromptVersion());
  }

  @Test
  @DisplayName(
      "given pending generation past its deadline, when a draft is accepted then it fails with"
          + " timeout")
  void givenPendingGenerationPastDeadlineWhenDraftAcceptedThenFailsWithTimeout() {
    var generation = GenerationFactory.pending();

    generation.acceptDraft(
        GenerationFactory.draft(), MODEL, PROMPT_VERSION, REQUESTED_AT.plusSeconds(90));

    assertEquals(GenerationStatus.FAILED, generation.getStatus());
    assertEquals(GenerationFailureReason.TIMEOUT, generation.getFailureReason());
    assertNull(generation.getDraft());
  }

  @ParameterizedTest
  @EnumSource(GenerationFailureReason.class)
  @DisplayName("given pending generation, when it fails then it records the reason")
  void givenPendingGenerationWhenFailThenRecordsReason(GenerationFailureReason reason) {
    var generation = GenerationFactory.pending();
    var failedAt = REQUESTED_AT.plusSeconds(5);

    generation.fail(reason, failedAt);

    assertEquals(GenerationStatus.FAILED, generation.getStatus());
    assertEquals(reason, generation.getFailureReason());
    assertEquals(failedAt, generation.getCompletedAt());
  }

  @Test
  @DisplayName(
      "given pending generation past its deadline, when it fails for another reason then the"
          + " reason is timeout")
  void givenPendingGenerationPastDeadlineWhenFailThenReasonIsTimeout() {
    var generation = GenerationFactory.pending();

    generation.fail(GenerationFailureReason.PROVIDER_ERROR, REQUESTED_AT.plusMinutes(5));

    assertEquals(GenerationFailureReason.TIMEOUT, generation.getFailureReason());
  }

  @Test
  @DisplayName("given an ended generation, when it is ended again then it throws")
  void givenEndedGenerationWhenEndedAgainThenThrows() {
    var generation = GenerationFactory.completed();
    var later = REQUESTED_AT.plusSeconds(30);

    assertThrows(
        IllegalStateException.class,
        () -> generation.fail(GenerationFailureReason.PROVIDER_ERROR, later));
    assertThrows(
        IllegalStateException.class,
        () -> generation.acceptDraft(GenerationFactory.draft(), MODEL, PROMPT_VERSION, later));
    assertEquals(GenerationStatus.COMPLETED, generation.getStatus());
  }

  @Test
  @DisplayName(
      "given pending generation, when checked just before and at 90 seconds then it times out"
          + " exactly at the deadline")
  void givenPendingGenerationWhenCheckedAroundDeadlineThenTimesOutAtDeadline() {
    var generation = GenerationFactory.pending();

    assertFalse(generation.isTimedOutAt(REQUESTED_AT.plusSeconds(89)));
    assertTrue(generation.isTimedOutAt(REQUESTED_AT.plusSeconds(90)));
  }

  @Test
  @DisplayName("given a completed generation, when checked long after then it is not timed out")
  void givenCompletedGenerationWhenCheckedLongAfterThenNotTimedOut() {
    assertFalse(GenerationFactory.completed().isTimedOutAt(REQUESTED_AT.plusDays(1)));
  }

  @Test
  @DisplayName(
      "given pending generation past its deadline, when observed then a failed timeout view is"
          + " returned and the generation itself stays pending")
  void givenPendingGenerationPastDeadlineWhenObservedThenTimeoutViewAndOriginalPending() {
    var generation = GenerationFactory.pending();

    var observed = generation.observedAt(REQUESTED_AT.plusMinutes(2));

    assertEquals(GenerationStatus.FAILED, observed.getStatus());
    assertEquals(GenerationFailureReason.TIMEOUT, observed.getFailureReason());
    assertEquals(REQUESTED_AT.plusSeconds(90), observed.getCompletedAt());
    assertEquals(generation.getId(), observed.getId());
    assertEquals(GenerationStatus.PENDING, generation.getStatus());
  }

  @Test
  @DisplayName("given pending generation within its deadline, when observed then it is unchanged")
  void givenPendingGenerationWithinDeadlineWhenObservedThenSameInstance() {
    var generation = GenerationFactory.pending();

    assertSame(generation, generation.observedAt(REQUESTED_AT.plusSeconds(30)));
  }

  @Test
  @DisplayName("given a completed state without a draft, when built then it throws")
  void givenCompletedStateWithoutDraftWhenBuildThenThrows() {
    var builder =
        VacancyDescriptionGeneration.builder()
            .vacancyId(VACANCY_ID)
            .input(GenerationFactory.input())
            .requestedAt(REQUESTED_AT)
            .status(GenerationStatus.COMPLETED)
            .completedAt(REQUESTED_AT);

    var exception = assertThrows(NullPointerException.class, builder::build);

    assertEquals("draft is required when COMPLETED", exception.getMessage());
  }

  @Test
  @DisplayName("given a pending state with a failure reason, when built then it throws")
  void givenPendingStateWithFailureReasonWhenBuildThenThrows() {
    var builder =
        VacancyDescriptionGeneration.builder()
            .vacancyId(VACANCY_ID)
            .input(GenerationFactory.input())
            .requestedAt(REQUESTED_AT)
            .status(GenerationStatus.PENDING)
            .failureReason(GenerationFailureReason.TIMEOUT);

    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  @DisplayName("given a failed state without a reason, when built then it throws")
  void givenFailedStateWithoutReasonWhenBuildThenThrows() {
    var builder =
        VacancyDescriptionGeneration.builder()
            .vacancyId(VACANCY_ID)
            .input(GenerationFactory.input())
            .requestedAt(REQUESTED_AT)
            .status(GenerationStatus.FAILED)
            .completedAt(REQUESTED_AT);

    assertThrows(NullPointerException.class, builder::build);
  }

  @Test
  @DisplayName("given two generations with the same id, when compared then they are equal")
  void givenSameIdWhenEqualsThenTrue() {
    var generation = GenerationFactory.pending();
    var copy =
        VacancyDescriptionGeneration.builder()
            .id(generation.getId())
            .vacancyId(VACANCY_ID)
            .input(GenerationFactory.input())
            .requestedAt(REQUESTED_AT)
            .status(GenerationStatus.PENDING)
            .build();

    assertEquals(generation, copy);
    assertEquals(generation.hashCode(), copy.hashCode());
  }

  @Test
  @DisplayName("given a generation, when toString then the answers are not included")
  void givenGenerationWhenToStringThenAnswersExcluded() {
    var text = GenerationFactory.completed().toString();

    assertFalse(text.contains("Visiting customers"));
    assertFalse(text.contains("A summary"));
  }
}
