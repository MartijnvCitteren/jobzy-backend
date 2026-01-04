package com.jobly_jobs.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationRegex {
    public static final String VALID_WEBSITE_REGEX = "^(https?://www\\.|https?://|www\\.)[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-z]{2,}(/.*)?$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_REGEX = "^[+\\d][\\d\\s-]{10,}$";
}
