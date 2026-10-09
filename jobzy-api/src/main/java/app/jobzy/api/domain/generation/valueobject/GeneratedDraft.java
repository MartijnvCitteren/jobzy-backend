package app.jobzy.api.domain.generation.valueobject;

import app.jobzy.api.shared.validation.TextContentRules;
import java.util.Objects;

/**
 * The texts a model drafted for a vacancy description. Only the sections the model has facts for
 * are generated; {@code whatWeOffer} and {@code aboutUs} are deliberately absent until company data
 * exists, so the model is never asked to invent them.
 *
 * <p>The texts are derived from personal data (the answers) and may repeat it, so {@link
 * #toString()} leaves them out.
 */
public record GeneratedDraft(String summary, String jobDescription, String tasks) {

  /** Same limits as the contract's {@code VacancyDescriptionRequest}. */
  public static final int SUMMARY_MAX_LENGTH = 1000;

  public static final int JOB_DESCRIPTION_MAX_LENGTH = 5000;
  public static final int TASKS_MAX_LENGTH = 5000;

  public GeneratedDraft {
    Objects.requireNonNull(summary, "summary is required");
    Objects.requireNonNull(jobDescription, "jobDescription is required");
    Objects.requireNonNull(tasks, "tasks is required");
  }

  /**
   * Whether this draft could be saved as-is through the manual description endpoint: every text is
   * non-blank, within the contract's length limit and passes the same content rules as user input.
   * A draft the user could not save is useless to them and a sign that the output was manipulated,
   * so it is never shown.
   */
  public boolean isSaveableAsDescription() {
    return isSaveable(summary, SUMMARY_MAX_LENGTH)
        && isSaveable(jobDescription, JOB_DESCRIPTION_MAX_LENGTH)
        && isSaveable(tasks, TASKS_MAX_LENGTH);
  }

  private static boolean isSaveable(String text, int maxLength) {
    return !text.isBlank() && text.length() <= maxLength && TextContentRules.isSafe(text);
  }

  @Override
  public String toString() {
    return "GeneratedDraft{summaryLength="
        + summary.length()
        + ", jobDescriptionLength="
        + jobDescription.length()
        + ", tasksLength="
        + tasks.length()
        + '}';
  }
}
