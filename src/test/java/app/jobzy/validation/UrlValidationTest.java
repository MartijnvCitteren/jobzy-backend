package app.jobzy.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UrlValidationTest {

  private UrlValidation urlValidation;

  @BeforeEach
  void setUp() {
    HttpClient httpClient = HttpClient.newHttpClient();
    urlValidation = new UrlValidation(httpClient);
  }

  @ParameterizedTest
  @CsvSource({"https://www.google.com, full https url with existing domain", "www.google.com, www.address.extension" +
      " format", "https://about.google/, url with trailing slash", "http://www.google.com, url with http " +
      "protocol", "'  www.google.com  ', url with leading and trailing whitespace", "WwW.GoOgLe.CoM, url with " +
      "mixed case", "https://www.google.com/search, url with path", "https://www.google.com/search?q=test, url " +
      "with query parameters", "https://google.com, url without www", "https://mail.google.com, subdomain url"})
  @DisplayName("given valid url with different formatting, then return true")
  void testUrlFormattingVariations(String url, String description) {
    assertTrue(urlValidation.isValid(url), "Failed for: " + description);
  }

  @ParameterizedTest
  @CsvSource({"'ht!tp://invalid url with spaces', url with invalid syntax and spaces", "'https://', url without " +
      "host", "'https://[invalid', url with invalid bracket", "'htp://typo.com', url with typo in protocol"})
  @DisplayName("given invalid url formats, then return false")
  void testInvalidUrlFormats(String url, String description) {
    assertFalse(urlValidation.isValid(url), "Should fail for: " + description);
  }

  @Test
  @DisplayName("given url with invalid host, then return false")
  void testUrlWithInvalidHost() {
    var url = "https://thishostdefinitelydoesnotexist12345.com";
    assertFalse(urlValidation.isValid(url));
  }

  @Test
  @DisplayName("given url with valid syntax but unreachable host, then return false")
  void testUnreachableHost() {
    var url = "www.thisdomaindoesnotexist999888777.com";
    assertFalse(urlValidation.isValid(url));
  }

  @Test
  @DisplayName("given url that times out, then return false")
  void testUrlTimeout() {
    // Using a non-routable IP address that will timeout
    var url = "http://10.255.255.1";
    assertFalse(urlValidation.isValid(url));
  }

  @Test
  @DisplayName("given url with special characters in domain, then handle correctly")
  void testUrlWithSpecialCharacters() {
    var url = "https://www.google.com/search?q=test&lang=en";
    assertTrue(urlValidation.isValid(url));
  }

  @Test
  @DisplayName("given url starting with http (not https), then format and validate correctly")
  void testHttpProtocolHandling() {
    var url = "http://www.example.com";
    assertTrue(urlValidation.isValid(url));
  }

  @Test
  @DisplayName("given url without protocol prefix, then add https prefix and validate")
  void testUrlWithoutProtocolPrefix() {
    var url = "www.google.com";
    assertTrue(urlValidation.isValid(url));
  }

}