package app.jobzy.api.application.port.in.command;

public enum VacancyCategoryDto {
  ADMINISTRATION(1),

  CONSTRUCTION(2),

  CUSTOMER_SUPPORT(3),

  DESIGN_CREATIVE(4),

  EDUCATION(5),

  ENGINEERING(6),

  FACILITY_SERVICES(7),

  FINANCE(8),

  HEALTHCARE(9),

  HOSPITALITY(10),

  HUMAN_RESOURCES(11),

  LEGAL(12),

  LOGISTICS_SUPPLY_CHAIN(13),

  MANAGEMENT(14),

  MARKETING(15),

  OPERATIONS(16),

  PRODUCTION_MANUFACTURING(17),

  RETAIL(18),

  SALES(19),

  SCIENCE_RESEARCH(20),

  OTHER(999);

  private final int categoryNumber;

  private VacancyCategoryDto(int categoryNumber) {
    this.categoryNumber = categoryNumber;
  }

  public int getCategoryNumber() {
    return categoryNumber;
  }

  public static VacancyCategoryDto fromCategoryNumber(int categoryNumber) {
    for (VacancyCategoryDto category : values()) {
      if (category.categoryNumber == categoryNumber) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown vacancy category number: " + categoryNumber);
  }
}
