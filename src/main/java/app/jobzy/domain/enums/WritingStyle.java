package app.jobzy.domain.enums;

import lombok.Getter;

@Getter
public enum WritingStyle {
  FORMAL("Use a formal and professional, formal tone with precise language and no informal expressions."),
  BUSINESS_CASUAL("Use a relaxed but professional tone that is approachable while remaining business-appropriate."),
  CASUAL("Use an informal, casual, conversational tone that feels natural and friendly."),
  CREATIVE("Use an expressive, creative, imaginative tone with engaging language and creative freedom."),
  TECHNICAL("Use a clear, precise, and factual tone focused on technical accuracy and domain-specific terminology.");

  WritingStyle(String description) {
    this.description = description;
  }
  private final String description;



}
