package app.jobzy.api.domain.vacancy.valueobject;

import org.jspecify.annotations.Nullable;

/**
 * Vacancy description: five optional text fields plus the source (MANUAL/GENERATED). Plain data
 * holder with no validation logic — content-safety checks (raw HTML, control characters) are
 * performed at the REST adapter edge before this object is constructed.
 */
public record VacancyDescription(
    @Nullable String summary,
    @Nullable String jobDescription,
    @Nullable String tasks,
    @Nullable String whatWeOffer,
    @Nullable String aboutUs,
    VacancyDescriptionSource source) {}
