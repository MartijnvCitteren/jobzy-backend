package app.jobzy.api.domain.identity.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.jobzy.api.domain.identity.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

  @ParameterizedTest
  @ValueSource(ints = {12, 64, 128})
  @DisplayName("given a password within 12 to 128 characters, when created then it is accepted")
  void givenPasswordWithinLengthBoundsWhenCreatedThenItIsAccepted(int length) {
    var value = "a".repeat(length);

    assertEquals(value, new Password(value).value());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 11, 129})
  @DisplayName("given a password outside 12 to 128 characters, when created then it is rejected")
  void givenPasswordOutsideLengthBoundsWhenCreatedThenItIsRejected(int length) {
    var value = "a".repeat(length);

    assertThrows(InvalidPasswordException.class, () -> new Password(value));
  }

  @Test
  @DisplayName(
      "given a password of 11 emoji, when created then it is rejected because emoji count as one"
          + " character each")
  void givenPasswordOfElevenEmojiWhenCreatedThenItIsRejected() {
    var elevenEmoji = "😀".repeat(11);

    assertThrows(InvalidPasswordException.class, () -> new Password(elevenEmoji));
  }

  @Test
  @DisplayName("given a password, when toString then the password is not in the output")
  void givenPasswordWhenToStringThenThePasswordIsNotInTheOutput() {
    var password = new Password("correct horse battery staple");

    assertFalse(password.toString().contains("horse"));
  }
}
