package app.jobzy.validation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Log4j2
@RequiredArgsConstructor
public class UrlValidation {

  private final HttpClient httpClient;

  public boolean isValid(String url) {
    if (ObjectUtils.isEmpty(url)) {
      return false;
    }
    url = formatUrl(url);
    return urlSyntaxValid(url) && hostExists(url) && websiteIsReachable(url);

  }

  private String formatUrl(String url) {
    url = url.trim().toLowerCase();
    if (doesNotStartWithHttp(url)) {
      url = "https://" + url;
    }
    return url;
  }

  private boolean urlSyntaxValid(String url) {
    try {
      URI uri = new URI(url);
      return (!ObjectUtils.isEmpty(uri.getHost()) && !ObjectUtils.isEmpty(uri.getScheme()));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private boolean hostExists(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host == null) {
        return false;
      }
      InetAddress.getByName(host);
      return true;
    } catch (URISyntaxException | UnknownHostException e) {
      return false;
    }
  }

  private boolean websiteIsReachable(String url) {
    try {
      HttpRequest head = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .method("HEAD", HttpRequest.BodyPublishers.noBody())
          .timeout(Duration.ofSeconds(2))
          .build();

      HttpResponse<Void> headResponse = httpClient.send(head, HttpResponse.BodyHandlers.discarding());

      if (indicatesWebsiteIsReachable(headResponse.statusCode())) {
        return true;
      }

      HttpRequest get = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .GET()
          .timeout(Duration.ofSeconds(2))
          .build();
      HttpResponse<Void> getResponse = httpClient.send(get, HttpResponse.BodyHandlers.discarding());

      return indicatesWebsiteIsReachable(getResponse.statusCode());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Thread interrupted during URL validation: {}", e.getMessage());
      return false;
    } catch (IOException e) {
      log.info("URL validation failed: {}", e.getMessage());
      return false;
    }

  }

  private boolean doesNotStartWithHttp(String url) {
    return !ObjectUtils.isEmpty(url) && !(url.startsWith("http://") || url.startsWith("https://"));
  }

  private boolean indicatesWebsiteIsReachable(int statusCode) {
    return statusCode != HttpStatus.NOT_FOUND.value() && statusCode != HttpStatus.GONE.value() && statusCode < 500;
  }
}
