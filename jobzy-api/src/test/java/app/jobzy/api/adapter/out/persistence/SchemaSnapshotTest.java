package app.jobzy.api.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Guards against unnoticed database schema changes: Hibernate generates the SQL Server DDL for the
 * JPA entities, exactly as Spring Boot configures them (naming strategies included), and the test
 * compares it with the committed {@code schema-snapshot.sql}. Generation is offline: the dialect
 * and database version are fixed, so no SQL Server is needed and nothing is executed.
 *
 * <p>On an intended schema change, review the generated DDL in {@code target/schema-snapshot.sql}
 * and copy it over {@code src/test/resources/schema-snapshot.sql}; the PR then needs the {@code
 * schema-change} label. The snapshot becomes the first Flyway migration later (ADR 0001).
 */
@ActiveProfiles("test")
@DataJpaTest(
    properties = {
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect",
      "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
      "spring.jpa.properties.jakarta.persistence.database-product-name=Microsoft SQL Server",
      // SQL Server 2025, the version in docker-compose.yaml.
      "spring.jpa.properties.jakarta.persistence.database-major-version=17",
      "spring.jpa.properties.jakarta.persistence.schema-generation.database.action=none",
      "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
      "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target="
          + SchemaSnapshotTest.GENERATED,
      "spring.jpa.properties.hibernate.hbm2ddl.schema-generation.script.append=false",
      "spring.jpa.properties.hibernate.hbm2ddl.delimiter=;",
      "spring.jpa.properties.hibernate.format_sql=true"
    })
class SchemaSnapshotTest {

  static final String GENERATED = "target/schema-snapshot.sql";
  private static final String SNAPSHOT = "/schema-snapshot.sql";

  @Test
  @DisplayName(
      "given the JPA entities, when Hibernate generates the SQL Server DDL, then it equals the"
          + " committed schema snapshot")
  void givenJpaEntitiesWhenSqlServerDdlGeneratedThenEqualsCommittedSnapshot() throws IOException {
    var generated = normalize(Files.readString(Path.of(GENERATED)));
    var snapshot = normalize(readSnapshot());

    assertEquals(
        snapshot,
        generated,
        "The database schema changed. If intended, review %s, copy it to src/test/resources%s and"
                .formatted(GENERATED, SNAPSHOT)
            + " label the PR 'schema-change'.");
  }

  private static String readSnapshot() throws IOException {
    try (var in =
        Objects.requireNonNull(
            SchemaSnapshotTest.class.getResourceAsStream(SNAPSHOT), SNAPSHOT + " is missing")) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /** Ignores line endings, trailing whitespace and blank lines. */
  private static String normalize(String ddl) {
    return ddl.lines()
        .map(String::stripTrailing)
        .filter(line -> !line.isEmpty())
        .collect(Collectors.joining("\n"));
  }
}
