package app.jobzy.api.adapter.out.emaildomain;

import app.jobzy.api.application.port.out.PublicEmailDomains;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * The public mail domain block list, read once at startup from {@code
 * identity/public-email-domains.txt} on the classpath, so the list can be extended without code.
 *
 * <p>One domain per line; {@code #} starts a comment. An entry ending in {@code .*} matches the
 * name under every top-level domain ({@code hotmail.*} matches {@code hotmail.com} and {@code
 * hotmail.co.uk}), because the big providers register their name in many countries.
 */
@Component
public final class ClasspathPublicEmailDomains implements PublicEmailDomains {
  static final String RESOURCE = "identity/public-email-domains.txt";
  private static final String ANY_TOP_LEVEL_DOMAIN = "*";

  private final Set<String> domains = new HashSet<>();
  private final List<String> namesUnderAnyTopLevelDomain;

  public ClasspathPublicEmailDomains() {
    this(new ClassPathResource(RESOURCE));
  }

  ClasspathPublicEmailDomains(Resource resource) {
    var entries = readEntries(resource);
    entries.stream().filter(entry -> !entry.endsWith(ANY_TOP_LEVEL_DOMAIN)).forEach(domains::add);
    namesUnderAnyTopLevelDomain =
        entries.stream()
            .filter(entry -> entry.endsWith(ANY_TOP_LEVEL_DOMAIN))
            .map(entry -> entry.substring(0, entry.length() - ANY_TOP_LEVEL_DOMAIN.length()))
            .toList();
  }

  @Override
  public boolean isPublic(String domain) {
    String normalized = domain.toLowerCase(Locale.ROOT);
    return domains.contains(normalized)
        || namesUnderAnyTopLevelDomain.stream().anyMatch(normalized::startsWith);
  }

  private static List<String> readEntries(Resource resource) {
    try (var reader =
        new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      return reader
          .lines()
          .map(line -> line.replaceFirst("#.*", "").trim().toLowerCase(Locale.ROOT))
          .filter(line -> !line.isEmpty())
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read the public e-mail domain list " + RESOURCE, e);
    }
  }
}
