package app.jobzy.api.domain.vacancy.valueobject;

/**
 * Vacancy description: five optional text fields plus the source (MANUAL/GENERATED). Plain data
 * holder with no validation logic — content-safety checks (raw HTML, control characters) are
 * performed at the REST adapter edge before this object is constructed.
 */
public record VacancyDescription(
    String summary,
    String jobDescription,
    String tasks,
    String whatWeOffer,
    String aboutUs,
    VacancyDescriptionSource source) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String summary;
    private String jobDescription;
    private String tasks;
    private String whatWeOffer;
    private String aboutUs;
    private VacancyDescriptionSource source;

    public Builder summary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder jobDescription(String jobDescription) {
      this.jobDescription = jobDescription;
      return this;
    }

    public Builder tasks(String tasks) {
      this.tasks = tasks;
      return this;
    }

    public Builder whatWeOffer(String whatWeOffer) {
      this.whatWeOffer = whatWeOffer;
      return this;
    }

    public Builder aboutUs(String aboutUs) {
      this.aboutUs = aboutUs;
      return this;
    }

    public Builder source(VacancyDescriptionSource source) {
      this.source = source;
      return this;
    }

    public VacancyDescription build() {
      return new VacancyDescription(summary, jobDescription, tasks, whatWeOffer, aboutUs, source);
    }
  }
}
