package app.jobzy.api.adapter.in.rest.vacancy.validation;

import app.jobzy.api.adapter.in.rest.vacancy.InvalidVacancyDescriptionRequestException;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionRequest;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Validates vacancy description fields at the REST adapter edge (fail-fast, before any
 * domain/application object construction). Rejects raw HTML/script-like tags and disallowed control
 * characters. Lives at the adapter layer, not in the domain, because validation should happen at
 * the entry point where the request is first received, not deep inside domain object construction.
 */
@Component
public class VacancyDescriptionContentValidator {

  private static final Pattern RAW_TAG = Pattern.compile("</?[a-zA-Z!][^>]*>?");
  private static final Pattern DISALLOWED_CONTROL_CHARACTER =
      Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F-\\x9F]");

  /**
   * Validates each field in the request, throwing if any contains raw HTML-like tags or control
   * characters (except allowed whitespace: newline, carriage return, tab).
   *
   * @param request the request to validate
   * @throws InvalidVacancyDescriptionRequestException if validation fails, naming the offending
   *     field
   */
  public void validate(VacancyDescriptionRequest request) {
    validateField("summary", request.getSummary());
    validateField("jobDescription", request.getJobDescription());
    validateField("tasks", request.getTasks());
    validateField("whatWeOffer", request.getWhatWeOffer());
    validateField("aboutUs", request.getAboutUs());
  }

  private void validateField(String fieldName, String value) {
    if (value == null) {
      return;
    }
    if (RAW_TAG.matcher(value).find()) {
      throw new InvalidVacancyDescriptionRequestException(
          fieldName, fieldName + " must not contain raw HTML or script-like tags");
    }
    if (DISALLOWED_CONTROL_CHARACTER.matcher(value).find()) {
      throw new InvalidVacancyDescriptionRequestException(
          fieldName, fieldName + " must not contain disallowed control characters");
    }
  }
}
