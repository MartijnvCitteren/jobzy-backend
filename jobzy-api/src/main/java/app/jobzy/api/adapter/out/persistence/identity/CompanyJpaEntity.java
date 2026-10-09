package app.jobzy.api.adapter.out.persistence.identity;

import app.jobzy.api.adapter.out.persistence.BaseJpaEntity;
import app.jobzy.api.shared.gdpr.ProcessData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for companies. The e-mail domain is unique: it is how a registrant is matched to their
 * company, so two companies on one domain would make that match ambiguous.
 */
@Getter
@Setter
@Entity
@Table(name = "company")
public class CompanyJpaEntity extends BaseJpaEntity {
  @Id private UUID id;

  @ProcessData
  @Column(nullable = false)
  private String name;

  @ProcessData
  @Column(nullable = false, unique = true)
  private String emailDomain;
}
