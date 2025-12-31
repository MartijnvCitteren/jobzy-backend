package com.jobly_jobs.constants;

public class AppConstants {
    public final static String VALID_WEBSITE_REGEX = "^$|" +
            "^(https?:\\/\\/)(www\\.)?[a-zA-Z0-9-]+\\." +
            "(nl|com|be|de|eu|io|tech|online|blog|net|info|org|nu|co\\.uk|fr|uk)" +
            "(\\/.*)?$" +
            "|^www\\.[a-zA-Z0-9-]+\\." +
            "(nl|com|be|de|eu|io|tech|online|blog|net|info|org|nu|co\\.uk|fr|uk)" +
            "(\\/.*)?$";

}
