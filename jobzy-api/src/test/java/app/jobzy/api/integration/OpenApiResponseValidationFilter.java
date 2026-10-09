package app.jobzy.api.integration;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialects;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * RestAssured filter that fails a test when a response does not match the OpenAPI contract: the
 * operation and status code must be documented, the content type must be one the contract declares
 * for that status, and the body must validate against its schema (formats included).
 *
 * <p>Validation is strict about undocumented properties: every object schema that lists {@code
 * properties} is treated as closed ({@code unevaluatedProperties: false}), so a field the contract
 * does not declare (for example leaked personal data) fails the test. This only applies to the
 * in-memory copy used here; the contract itself is not changed.
 *
 * <p>Only responses are validated. Requests are not, because tests deliberately send requests that
 * violate the contract to check that they are rejected.
 */
public final class OpenApiResponseValidationFilter implements Filter {

  /** System property with the path of the contract, set by Surefire from {@code pom.xml}. */
  static final String SPEC_PROPERTY = "jobzy.contract.vacancy-api";

  private static final String SPEC_IRI = "urn:jobzy:contract:vacancy-api";

  private static volatile OpenApiResponseValidationFilter instance;

  private final JsonNode spec;
  private final SchemaRegistry schemaRegistry;
  private final Map<Pattern, String> pathPatterns;

  private OpenApiResponseValidationFilter(JsonNode spec) {
    this.spec = spec;
    var closedSpec = closeObjectSchemas(spec.deepCopy());
    this.schemaRegistry =
        SchemaRegistry.withDefaultDialect(
            Dialects.getOpenApi31(),
            builder ->
                builder
                    .schemas(Map.of(SPEC_IRI, closedSpec.toString()))
                    .schemaRegistryConfig(
                        SchemaRegistryConfig.builder().formatAssertionsEnabled(true).build()));
    this.pathPatterns =
        spec.required("paths").propertyNames().stream()
            .collect(Collectors.toMap(OpenApiResponseValidationFilter::toPattern, path -> path));
  }

  /** Returns the filter for the vacancy contract, parsing it once per test JVM. */
  public static OpenApiResponseValidationFilter vacancyApi() {
    var result = instance;
    if (result == null) {
      synchronized (OpenApiResponseValidationFilter.class) {
        result = instance;
        if (result == null) {
          result = new OpenApiResponseValidationFilter(readSpec(specPath()));
          instance = result;
        }
      }
    }
    return result;
  }

  @Override
  public Response filter(
      FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec,
      FilterContext ctx) {
    var response = ctx.next(requestSpec, responseSpec);
    var path = stripPrefix(requestSpec.getDerivedPath(), requestSpec.getBasePath());
    var violations =
        validate(
            requestSpec.getMethod(),
            path,
            response.getStatusCode(),
            response.getContentType(),
            response.getBody().asString());
    if (!violations.isEmpty()) {
      throw new AssertionError(
          "Response of %s %s (%d) violates the OpenAPI contract:%n  - %s%nBody: %s"
              .formatted(
                  requestSpec.getMethod(),
                  path,
                  response.getStatusCode(),
                  String.join("%n  - ".formatted(), violations),
                  response.getBody().asString()));
    }
    return response;
  }

  /**
   * Validates one response against the contract and returns the violations, empty if it conforms.
   */
  List<String> validate(String method, String path, int status, String contentType, String body) {
    var templatePath = matchPath(path);
    if (templatePath.isEmpty()) {
      return List.of("path %s is not in the contract".formatted(path));
    }
    var operationPointer =
        "/paths/" + escape(templatePath.get()) + "/" + method.toLowerCase(Locale.ROOT);
    if (spec.at(operationPointer).isMissingNode()) {
      return List.of(
          "operation %s %s is not in the contract".formatted(method, templatePath.get()));
    }

    var responsePointer = resolveResponse(operationPointer + "/responses", status);
    if (responsePointer.isEmpty()) {
      return List.of("status %d is not documented for this operation".formatted(status));
    }

    var content = spec.at(responsePointer.get() + "/content");
    if (content.isMissingNode() || content.isEmpty()) {
      return body.isEmpty()
          ? List.of()
          : List.of("contract declares no body, but one was returned");
    }

    if (body.isEmpty()) {
      return List.of("contract declares a body, but none was returned");
    }
    var mediaType = mediaType(contentType);
    if (!content.has(mediaType)) {
      return List.of(
          "content type '%s' is not one of %s".formatted(mediaType, content.propertyNames()));
    }

    var schemaPointer = responsePointer.get() + "/content/" + escape(mediaType) + "/schema";
    Schema schema = schemaRegistry.getSchema(SchemaLocation.of(SPEC_IRI + "#" + schemaPointer));
    return schema.validate(body, InputFormat.JSON).stream().map(Error::toString).toList();
  }

  /** Finds the contract path for a request path; a literal segment wins over a template. */
  private Optional<String> matchPath(String path) {
    return pathPatterns.entrySet().stream()
        .filter(entry -> entry.getKey().matcher(path).matches())
        .map(Map.Entry::getValue)
        .min(
            Comparator.comparingLong(
                templatePath -> templatePath.chars().filter(c -> c == '{').count()));
  }

  /** Finds the response for the status code (or {@code default}), following a {@code $ref}. */
  private Optional<String> resolveResponse(String responsesPointer, int status) {
    var pointer = responsesPointer + "/" + status;
    if (spec.at(pointer).isMissingNode()) {
      pointer = responsesPointer + "/default";
    }
    var response = spec.at(pointer);
    if (response.isMissingNode()) {
      return Optional.empty();
    }
    var ref = response.path("$ref");
    return Optional.of(ref.isString() ? ref.asString().substring(1) : pointer);
  }

  /**
   * Adds {@code unevaluatedProperties: false} to every object schema that lists {@code properties}
   * and does not say otherwise, so undocumented response fields fail validation.
   */
  private static JsonNode closeObjectSchemas(JsonNode node) {
    if (node instanceof ObjectNode object) {
      if (object.has("properties")
          && object.get("properties").isObject()
          && !object.has("additionalProperties")
          && !object.has("unevaluatedProperties")) {
        object.put("unevaluatedProperties", false);
      }
      object.values().forEach(OpenApiResponseValidationFilter::closeObjectSchemas);
    } else if (node.isArray()) {
      node.values().forEach(OpenApiResponseValidationFilter::closeObjectSchemas);
    }
    return node;
  }

  private static Pattern toPattern(String templatePath) {
    var regex =
        Pattern.compile("\\{[^/}]+}")
            .splitAsStream(templatePath)
            .map(Pattern::quote)
            .collect(Collectors.joining("[^/]+"));
    return Pattern.compile(templatePath.endsWith("}") ? regex + "[^/]+" : regex);
  }

  /** Strips the servlet context path, which the contract declares as its server URL. */
  private static String stripPrefix(String path, String basePath) {
    return path.startsWith(basePath) ? path.substring(basePath.length()) : path;
  }

  private static String mediaType(String contentType) {
    return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
  }

  /** Escapes a JSON Pointer reference token (RFC 6901). */
  private static String escape(String token) {
    return token.replace("~", "~0").replace("/", "~1");
  }

  private static Path specPath() {
    var value = System.getProperty(SPEC_PROPERTY);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "System property %s is not set; run the tests through Maven (Surefire sets it)"
              .formatted(SPEC_PROPERTY));
    }
    return Path.of(value.strip());
  }

  private static JsonNode readSpec(Path specPath) {
    try {
      return YAMLMapper.builder().build().readTree(Files.readString(specPath));
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read the OpenAPI contract " + specPath, e);
    }
  }
}
