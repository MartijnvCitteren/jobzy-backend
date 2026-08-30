package app.jobzy.api.vacancy.domain;

public record Location(String country, String city) {

  public Location {
    if (country == null || country.isBlank()) {
      throw new IllegalArgumentException("Location requires a country");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("Location requires a city");
    }
  }
}
