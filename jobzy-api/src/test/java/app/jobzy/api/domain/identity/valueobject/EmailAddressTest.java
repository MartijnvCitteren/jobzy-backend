package app.jobzy.api.domain.identity.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.jobzy.api.domain.identity.InvalidEmailAddressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailAddressTest {

  @Test
  @DisplayName(
      "given an address with upper case and surrounding spaces, when created then it is trimmed"
          + " and lower case")
  void givenAddressWithUpperCaseAndSpacesWhenCreatedThenItIsTrimmedAndLowerCase() {
    var email = new EmailAddress("  Jane.Doe@ACME.nl ");

    assertEquals("jane.doe@acme.nl", email.value());
  }

  @Test
  @DisplayName("given a valid address, when domain then returns the part after the at sign")
  void givenValidAddressWhenDomainThenReturnsThePartAfterTheAtSign() {
    var email = new EmailAddress("jane@sales.acme.co.uk");

    assertEquals("sales.acme.co.uk", email.domain());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(
      strings = {
        "",
        "jane.doe",
        "@acme.nl",
        "jane@",
        "jane@acme",
        "jane@@acme.nl",
        "jane@doe@acme.nl",
        "jane doe@acme.nl",
        "jane@.acme.nl",
        "jane@acme.nl."
      })
  @DisplayName("given a malformed address, when created then it is rejected")
  void givenMalformedAddressWhenCreatedThenItIsRejected(String malformed) {
    assertThrows(InvalidEmailAddressException.class, () -> new EmailAddress(malformed));
  }

  @Test
  @DisplayName("given an address, when toString then the address is not in the output")
  void givenAddressWhenToStringThenTheAddressIsNotInTheOutput() {
    var email = new EmailAddress("jane.doe@acme.nl");

    assertFalse(email.toString().contains("jane"));
  }
}
