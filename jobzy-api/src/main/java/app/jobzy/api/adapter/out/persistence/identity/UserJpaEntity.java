package app.jobzy.api.adapter.out.persistence.identity;

import app.jobzy.api.adapter.out.persistence.BaseJpaEntity;
import app.jobzy.api.domain.identity.valueobject.JobRole;
import app.jobzy.api.domain.identity.valueobject.Membership;
import app.jobzy.api.domain.identity.valueobject.UserStatus;
import app.jobzy.api.shared.gdpr.PersonalData;
import app.jobzy.api.shared.gdpr.ProcessData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for users, in table {@code app_user} because {@code user} is a reserved word in SQL
 * Server. The company is referenced by id only, not as an association: company and user are
 * separate aggregates.
 *
 * <p>The verification token hash gets a plain index, not a unique constraint: SQL Server allows
 * only one {@code NULL} in a unique column, and every confirmed user has none.
 */
@Getter
@Setter
@Entity
@Table(
    name = "app_user",
    indexes = {
      @Index(name = "ix_app_user_company_id", columnList = "company_id"),
      @Index(name = "ix_app_user_verification_token_hash", columnList = "verification_token_hash")
    })
public class UserJpaEntity extends BaseJpaEntity {
  @Id private UUID id;

  @PersonalData
  @Column(nullable = false)
  private String firstName;

  @PersonalData
  @Column(nullable = false)
  private String lastName;

  @PersonalData
  @Column(nullable = false, unique = true)
  private String email;

  @ProcessData
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private JobRole jobRole;

  @ProcessData
  @Column(name = "company_id", nullable = false)
  private UUID companyId;

  @ProcessData
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Membership membership;

  @ProcessData
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @ProcessData
  @Column(name = "verification_token_hash")
  private String verificationTokenHash;

  @ProcessData private LocalDateTime verificationTokenExpiresAt;

  @OneToOne(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private UserCredentialsJpaEntity credentials;
}
