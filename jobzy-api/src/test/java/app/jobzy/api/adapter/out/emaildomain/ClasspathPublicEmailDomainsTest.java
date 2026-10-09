package app.jobzy.api.adapter.out.emaildomain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;

class ClasspathPublicEmailDomainsTest {

  private final ClasspathPublicEmailDomains shippedList = new ClasspathPublicEmailDomains();

  @ParameterizedTest
  @ValueSource(
      strings = {
        "gmail.com",
        "googlemail.com",
        "outlook.com",
        "hotmail.com",
        "hotmail.co.uk",
        "live.nl",
        "yahoo.fr",
        "icloud.com",
        "proton.me",
        "protonmail.com",
        "gmx.de",
        "ziggo.nl",
        "kpnmail.nl",
        "GMAIL.COM"
      })
  @DisplayName("given a public mail domain, when checked against the shipped list then public")
  void givenPublicMailDomainWhenCheckedAgainstShippedListThenPublic(String domain) {
    assertTrue(shippedList.isPublic(domain));
  }

  @ParameterizedTest
  @ValueSource(strings = {"acme.nl", "jobzy.app", "sub.gmail.com.acme.nl", "mygmail.com"})
  @DisplayName("given a company domain, when checked against the shipped list then not public")
  void givenCompanyDomainWhenCheckedAgainstShippedListThenNotPublic(String domain) {
    assertFalse(shippedList.isPublic(domain));
  }

  @Test
  @DisplayName(
      "given a list with comments, blank lines and upper case, when read then only the entries"
          + " count")
  void givenListWithCommentsBlankLinesAndUpperCaseWhenReadThenOnlyTheEntriesCount() {
    var list =
        new ClasspathPublicEmailDomains(
            new ByteArrayResource(
                "# comment\n\n  Example.ORG  # trailing comment\nfree.*\n"
                    .getBytes(StandardCharsets.UTF_8)));

    assertTrue(list.isPublic("example.org"));
    assertTrue(list.isPublic("free.fr"));
    assertFalse(list.isPublic("comment"));
    assertFalse(list.isPublic("freedom.fr"));
  }
}
