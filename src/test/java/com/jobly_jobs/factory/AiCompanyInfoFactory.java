package com.jobly_jobs.factory;

import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;

public class AiCompanyInfoFactory {

  public static CompanyInfoAiResponse createAiCompanyInfo() {
    return new CompanyInfoAiResponse("A leading technology company specializing in innovative software solutions",
        "To revolutionize the tech industry through cutting-edge software development",
        "Competitive salary, flexible working hours, continuous learning opportunities, and " +
            "a collaborative work environment",
        "Professional, innovative, and approachable");
  }

  public static CompanyInfoAiResponse createAiCompanyInfoWithCustomDescription(String description) {
    return new CompanyInfoAiResponse(description,
        "To revolutionize the tech industry through cutting-edge software development",
        "Competitive salary, flexible working hours, continuous learning opportunities",
        "Professional, innovative, and approachable");
  }
}

