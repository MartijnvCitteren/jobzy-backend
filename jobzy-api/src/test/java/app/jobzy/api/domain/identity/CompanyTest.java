package app.jobzy.api.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CompanyTest {
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 9, 12, 0);

  @Test
  @DisplayName(
      "given a name and an upper case domain, when founded then the domain is stored in lower case"
          + " and an id is generated")
  void givenNameAndUpperCaseDomainWhenFoundedThenDomainIsLowerCaseAndIdIsGenerated() {
    var company = Company.found(" Acme ", "ACME.nl", NOW);

    assertNotNull(company.getId());
    assertEquals("Acme", company.getName());
    assertEquals("acme.nl", company.getEmailDomain());
    assertEquals(NOW, company.getCreatedAt());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "  "})
  @DisplayName("given a blank name, when founded then it is rejected")
  void givenBlankNameWhenFoundedThenItIsRejected(String name) {
    assertThrows(IllegalArgumentException.class, () -> Company.found(name, "acme.nl", NOW));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "  "})
  @DisplayName("given a blank e-mail domain, when founded then it is rejected")
  void givenBlankEmailDomainWhenFoundedThenItIsRejected(String emailDomain) {
    assertThrows(IllegalArgumentException.class, () -> Company.found("Acme", emailDomain, NOW));
  }

  @Test
  @DisplayName("given a builder with an explicit id, when built then the company has that id")
  void givenBuilderWithExplicitIdWhenBuiltThenTheCompanyHasThatId() {
    var id = UUID.randomUUID();

    var company =
        Company.builder().id(id).name("Acme").emailDomain("acme.nl").createdAt(NOW).build();

    assertEquals(id, company.getId());
  }

  @Test
  @DisplayName("given two companies, when compared then equality follows the id")
  void givenTwoCompaniesWhenComparedThenEqualityFollowsTheId() {
    var id = UUID.randomUUID();
    var company = Company.builder().id(id).name("Acme").emailDomain("acme.nl").createdAt(NOW);

    assertEquals(company.build(), company.build());
    assertEquals(company.build().hashCode(), company.build().hashCode());
    assertNotEquals(Company.found("Acme", "acme.nl", NOW), Company.found("Acme", "acme.nl", NOW));
  }
}
