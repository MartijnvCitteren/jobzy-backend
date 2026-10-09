package app.jobzy.api.domain.generation.valueobject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GeneratedDraftTest {

  @Test
  @DisplayName(
      "given texts exactly at their maximum lengths with newlines, when checked then saveable")
  void givenTextsAtMaximumLengthsWhenCheckedThenSaveable() {
    var draft =
        new GeneratedDraft(
            "s".repeat(GeneratedDraft.SUMMARY_MAX_LENGTH),
            "j\n".repeat(GeneratedDraft.JOB_DESCRIPTION_MAX_LENGTH / 2),
            "t".repeat(GeneratedDraft.TASKS_MAX_LENGTH));

    assertTrue(draft.isSaveableAsDescription());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unsaveableDrafts")
  @DisplayName("given a draft breaking a rule, when checked then not saveable")
  void givenDraftBreakingRuleWhenCheckedThenNotSaveable(String rule, GeneratedDraft draft) {
    assertFalse(draft.isSaveableAsDescription());
  }

  private static Stream<Arguments> unsaveableDrafts() {
    return Stream.of(
        arguments("blank summary", new GeneratedDraft(" ", "Job", "Tasks")),
        arguments("empty tasks", new GeneratedDraft("Summary", "Job", "")),
        arguments("summary too long", new GeneratedDraft("s".repeat(1001), "Job", "Tasks")),
        arguments("job description too long", new GeneratedDraft("S", "j".repeat(5001), "T")),
        arguments("tasks too long", new GeneratedDraft("S", "J", "t".repeat(5001))),
        arguments("raw tag", new GeneratedDraft("S", "<a href='x'>link</a>", "T")),
        arguments("control character", new GeneratedDraft("S", "J", "Task\u0000")));
  }

  @Test
  @DisplayName("given a missing text, when constructed then it throws")
  void givenMissingTextWhenConstructedThenThrows() {
    assertThrows(NullPointerException.class, () -> new GeneratedDraft("S", null, "T"));
  }

  @Test
  @DisplayName("given a draft, when toString then the texts are not included")
  void givenDraftWhenToStringThenTextsExcluded() {
    assertFalse(new GeneratedDraft("secret", "secret", "secret").toString().contains("secret"));
  }
}
