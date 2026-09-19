package app.jobzy.api.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
  public static final String AMS_TIME_ZONE_ID = "Europe/Amsterdam";

  public static final String VALIDATION_FAILED_TITLE = "Validation failed";
  public static final String VALIDATION_FAILED_DETAIL =
      "The request failed validation on one or more fields, see 'errors' for details.";
  public static final String NOT_FOUND_TITLE = "Not found";
  public static final String INTERNAL_SERVER_ERROR_TITLE = "Internal server error";
  public static final String INTERNAL_SERVER_ERROR_DETAIL =
      "An unexpected error occurred while processing the request";
}
