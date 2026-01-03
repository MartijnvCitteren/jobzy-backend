package com.jobly_jobs.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConstants {
    public static final String VALID_WEBSITE_REGEX = "^(https?://www\\.|https?://|www\\.)[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-z]{2,}(/.*)?$";
}
