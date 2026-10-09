package app.jobzy.api.domain.generation.valueobject;

import app.jobzy.api.domain.vacancy.valueobject.Language;
import java.util.Objects;

/**
 * Snapshot of everything a generation is based on: the vacancy's job title and language at the
 * moment the generation was started, plus the user's three answers. A snapshot, not a reference, so
 * a later change to the vacancy never changes what a generation was based on.
 *
 * <p>The three answers are free text about a team and its work and routinely name people, so they
 * are personal data; {@link #toString()} leaves them out to keep them out of logs.
 */
public record GenerationInput(
    String jobTitle, Language language, String mostImportantTasks, String team, String whyNiceJob) {

  public GenerationInput {
    Objects.requireNonNull(jobTitle, "jobTitle is required");
    Objects.requireNonNull(language, "language is required");
    Objects.requireNonNull(mostImportantTasks, "mostImportantTasks is required");
    Objects.requireNonNull(team, "team is required");
    Objects.requireNonNull(whyNiceJob, "whyNiceJob is required");
  }

  @Override
  public String toString() {
    return "GenerationInput{jobTitle='" + jobTitle + "', language=" + language + '}';
  }
}
