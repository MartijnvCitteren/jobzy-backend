package app.jobzy.api.domain.identity.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class RegistrantTest {
  private static final EmailAddress EMAIL = new EmailAddress("jane.doe@acme.nl");

  @Test
  @DisplayName("given names with surrounding spaces, when created then the names are trimmed")
  void givenNamesWithSurroundingSpacesWhenCreatedThenTheNamesAreTrimmed() {
    var registrant = new Registrant(" Jane ", " Doe ", EMAIL, JobRole.HR);

    assertEquals("Jane", registrant.firstName());
    assertEquals("Doe", registrant.lastName());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  @DisplayName("given a blank first name, when created then it is rejected")
  void givenBlankFirstNameWhenCreatedThenItIsRejected(String firstName) {
    assertThrows(
        IllegalArgumentException.class, () -> new Registrant(firstName, "Doe", EMAIL, JobRole.HR));
  }

  @Test
  @DisplayName("given a registrant, when toString then no personal data is in the output")
  void givenRegistrantWhenToStringThenNoPersonalDataIsInTheOutput() {
    var output = new Registrant("Jane", "Doe", EMAIL, JobRole.HR).toString();

    assertFalse(output.contains("Jane"));
    assertFalse(output.contains("Doe"));
    assertFalse(output.contains("acme"));
  }
}
