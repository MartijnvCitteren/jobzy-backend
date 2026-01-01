package com.jobly_jobs.factory;

import com.jobly_jobs.domain.dto.AiCompanyInfo;

public class AiCompanyInfoFactory {

    public static AiCompanyInfo createAiCompanyInfo() {
        return new AiCompanyInfo(
                "A leading technology company specializing in innovative software solutions",
                "To revolutionize the tech industry through cutting-edge software development",
                "Competitive salary, flexible working hours, continuous learning opportunities, and a collaborative work environment",
                "Professional, innovative, and approachable"
        );
    }

    public static AiCompanyInfo createAiCompanyInfoWithCustomDescription(String description) {
        return new AiCompanyInfo(
                description,
                "To revolutionize the tech industry through cutting-edge software development",
                "Competitive salary, flexible working hours, continuous learning opportunities",
                "Professional, innovative, and approachable"
        );
    }
}

