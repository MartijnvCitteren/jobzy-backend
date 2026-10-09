package app.jobzy.api.domain.identity;

import app.jobzy.api.domain.BaseObject;
import app.jobzy.api.domain.UuidV7Generator;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * A company that uses Jobzy. Identified by its e-mail domain: everyone who registers with an
 * address on that domain belongs to it, which is why the domain is unique and stored in lower case.
 * The first registrant of a domain founds the company and gives it its name.
 */
public class Company extends BaseObject {

  private final UUID id;
  private final String name;
  private final String emailDomain;

  private Company(UUID id, String name, String emailDomain, LocalDateTime createdAt) {
    super(createdAt);
    this.id = id;
    this.name = name;
    this.emailDomain = emailDomain;
  }

  /**
   * Creates a new company for an e-mail domain that has no company yet.
   *
   * @param name the company name, as given by the first registrant
   * @param emailDomain the e-mail domain of the first registrant
   * @param foundedAt when the first registrant registered
   */
  public static Company found(String name, String emailDomain, LocalDateTime foundedAt) {
    return builder().name(name).emailDomain(emailDomain).createdAt(foundedAt).build();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmailDomain() {
    return emailDomain;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Company company)) {
      return false;
    }
    return Objects.equals(getId(), company.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return "Company{id=" + id + ", emailDomain='" + emailDomain + "'}";
  }

  /** Builds a company; also used to load one from the database, with an explicit id. */
  public static class Builder {
    private UUID explicitId;
    private String name;
    private String emailDomain;
    private LocalDateTime createdAt;

    private Builder() {}

    public Builder id(UUID id) {
      this.explicitId = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder emailDomain(String emailDomain) {
      this.emailDomain = emailDomain;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Company build() {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("name is required");
      }
      if (emailDomain == null || emailDomain.isBlank()) {
        throw new IllegalArgumentException("emailDomain is required");
      }
      UUID id = explicitId != null ? explicitId : UuidV7Generator.getUUID();
      return new Company(
          id,
          name.trim(),
          emailDomain.trim().toLowerCase(Locale.ROOT),
          Objects.requireNonNull(createdAt, "createdAt is required"));
    }
  }
}
