package app.jobzy.api.domain.generation.valueobject;

/**
 * Why a generation ended in {@code FAILED}. The distinction matters to the user: only {@link
 * #INPUT_REJECTED} means the input has to change; for every other reason starting a new generation
 * with the same input may succeed.
 */
public enum GenerationFailureReason {
  /** The AI provider returned an error or could not be reached. */
  PROVIDER_ERROR,
  /** The generation did not finish in time, see {@code VacancyDescriptionGeneration}. */
  TIMEOUT,
  /** The model judged the input not to describe a job (off-topic or an injection attempt). */
  INPUT_REJECTED,
  /** The model's output could not be parsed or broke the content rules for a description. */
  INVALID_OUTPUT
}
