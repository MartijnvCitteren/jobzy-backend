package com.jobly_jobs.prompt.generator;

import com.jobly_jobs.domain.dto.request.JobCreationRequestDto;
import com.jobly_jobs.domain.enums.WritingStyle;
import org.springframework.stereotype.Component;

@Component
public class PromptCreator {

    public String createPrompt(JobCreationRequestDto inputDto) {
        String genericContext = createBasicPrompt(inputDto);
        return genericContext + createCompanyDescription(inputDto) + createTeamDescription(
                inputDto) + createDateToDayDescription(inputDto) + createJobDescription(
                inputDto) + createJobUniqueSellingPoints(inputDto) + createRequirements(inputDto) + createOffer(
                inputDto) + createContactInformation(inputDto) + createSummary(inputDto);
    }

    private String createBasicPrompt(JobCreationRequestDto inputDto) {
        return "Act like an experienced recruiter that has great vacancy writing skills. Your task is to write " + "a"
                + " " + "good vacancy" + "text that is appealing to applicants who have the job title you try to " +
                "attract. " + "As an experienced you understand the job market,  know a lot about SEO and " + "how " + "consumer psychology works. You return the vacancy text in a structured data format. This format " + "contains " + " The following elements: " + "1. Summary: a short summary of the job, this summary " + "applies the AIDA model and has the goal to grabs someonse attention. " + " and make someone " + "interested to keep on reading. " + "2. Company description: a short description of the company. " + "Explain here what they do and why they do it" + "3. Team description: a short description of the " + "team. Explain what the team looks like and what makes it interesting to work with them" + "4. Day to" + " day description: a short description of the day to day activities. Explain what the job looks like " + "on a daily basis" + " and what challenges and opportunities are." + "5. Job description: a short " + "description of the job. Explain what the job is about and what makes it interesting to do." + "6. " + "Job unique selling points: a short description of the unique selling points of the job. Explain what" + " makes this job unique and interesting." + "7. Requirements: a short description of the requirements" + " " + "for the job. Explain what the company is looking for in a candidate." + "This list should be " + "between 4 and 6 requirements. Where the most important skills are listed first." + "8. Offer: a list" + " description of the offer for the job. Explain what the company has to offer to the candidate." + "This list should be between 5 and 7 job benefits. Where the most important offers are listed first." + "9. Contact information: a short description of the contact information for the job. Explain how to apply for the job." + "Keep this structure in mind while writing the vacacytext and make sure there are NOT any repetitions in the text. " + "you get some basic information about the job. If you don't think you need the information for the " + "specific part you are writing, you can ignore it. " + "There is specific information you need to use in this text. The company name is: " + inputDto.generalInfo()
                .companyName() + " and the job title is: " + inputDto.generalInfo()
                .jobTitle() + "You have to write this vacancy text in this language: " + inputDto.language() + " and "
                + "in this writing style: " + inputDto.writingStyle() + writingStyleExample(
                inputDto.writingStyle()) + "Keep the text fairly easy to read and understand. Use short sentences " + "and" + " paragraphs. Aim to " + "write" + " on B2 or C1 level." + "Make sure the text is unique and " + "not " + "copied from other sources. " + "Also make " + "sure the text is about 1 A4 long. ";


    }

    private String createCompanyDescription(JobCreationRequestDto inputDto) {
        return "In this case write a company description and consider this " + "information as most important: " +
                "the" + " company is: " + inputDto.generalInfo()
                .companyName() + " First act like as a human recruiter and search the internet if you can find " +
                "information about " + "this company. " + "Make sure you find a website that's up to date. " + "And " + "written in the language the " + "vacancy text is written in." + " If you can't find " + "information" + " about the company, you can use " + "information that you already know. " + "If " + "you don't know " + "this company DO NOT make up information. " + "Don't write about the company " + "culture or " + "values if you don't know them. If you do know feel add" + " them in the text" + ". If you don't know" + " the company," + " you can write about the industry the company " + "is " + "in. you can this based " + "on the summary of the job" + inputDto.jobSummary() + " and the team " + "description: " + inputDto.teamDescription() + " Do not write explicitly about the team and " + "the job. This will be written in another part.";
    }

    private String createTeamDescription(JobCreationRequestDto inputDto) {
        return "In this case write a team description and consider this " + "information as most important: the " +
                "team looks the following: :" + inputDto.teamDescription() + " also consider this information: " + inputDto.tasks() + " and " + inputDto.skills() + " Focus on this part really on the team. The skills and tasks are just there for some context.";
    }

    private String createDateToDayDescription(JobCreationRequestDto inputDto) {
        return "In this case write a day to day description and consider this " + "information as most " + "important"
                + ": " + "the day to day activities are: :" + inputDto.tasks() + " also consider this " +
                "information:" + " " + inputDto.skills() + " and " + inputDto.teamDescription() + " Focus on this " + "part " + "really on " + "the day to day activities. The skills and team description are just " + "there for some " + "context.";
    }

    private String createJobDescription(JobCreationRequestDto inputDto) {
        return "In this case write a job description and consider this " + "information as most important: the " +
                "job" + " description is: :" + inputDto.tasks() + " also consider this information: " + inputDto.skills() + " and " + inputDto.teamDescription() + "this text is the body of the vacancy text so make it two alineas long" + " Focus on this part really on the job description. The skills and team description are just there " + "for some context.";

    }

    private String createJobUniqueSellingPoints(JobCreationRequestDto inputDto) {
        return "In this case write a job's unique selling points. There should a maximum of four USP's " + " and" +
                " " + "consider this " + "information as most important: the job unique selling points are: :" + inputDto.tasks() + " also consider this information: " + inputDto.skills() + " and " + inputDto.teamDescription() + "focus on the USP's and make this text 2-3 alineas long";
    }

    private String createRequirements(JobCreationRequestDto inputDto) {
        return "In this case write the requirements for the job and consider this " + "information as most " +
                "important: the requirements are: :" + inputDto.skills() + " also consider this information: " + inputDto.tasks() + " and " + inputDto.teamDescription() + "focus really on the requirements and make a bullet point list of the requirements " + "and order " + "them in order of importance. Generaly speaking start with education or work experience";
    }

    private String createOffer(JobCreationRequestDto inputDto) {
        return "In this case write the offer for the job and consider this " + "information as most important: " +
                "the" + " offer is: :" + inputDto.generalInfo()
                .minSalary() + " to " + inputDto.generalInfo()
                .maxSalary() + " also consider this information: " + inputDto.skills() + " and " + inputDto.teamDescription() + " and the company name is: " + inputDto.generalInfo()
                .companyName() + "really focus on the offer here, make a bullet point list of the offer and order " + "them in order of " + "importance";
    }

    private String createContactInformation(JobCreationRequestDto inputDto) {
        return "In this case write the contact information for the job and consider this " + "information as " +
                "most " + "important: the contact information is: :" + inputDto.generalInfo()
                .companyName() + " make this an alinea of 2-3 sentences";

    }

    private String createSummary(JobCreationRequestDto inputDto) {
        return "In this case write a summary for the job and consider this " + "information as most important: " +
                "the" + " summary is: :" + inputDto.jobSummary() + " make this an alinea of 2-3 sentences. " + "But " +
                "also use the information that is written before.";
    }


    private String writingStyleExample(WritingStyle style) {
        return switch (style) {
            case FORMAL ->
                    "You have to write in a formal style. This means you have to use formal language and avoid using "
                            + "contractions. For example, instead of writing 'you're' you have to write 'you are'.";
            case BUSINESS_CASUAL ->
                    "You have to write in a business casual style. This means you have to use a friendly tone and " + "avoid using jargon. For example, instead of writing 'synergy' you have to write 'working" + " together'.";
            case CASUAL ->
                    "You have to write in a casual style. This means you have to use a conversational tone and avoid "
                            + "using complex words. For example, instead of writing 'utilize' you have to write 'use'.";
            case CREATIVE ->
                    "You have to write in a creative style. This means you have to use imaginative language and " +
                            "avoid" + " using cliches. For example, instead of writing 'think outside the box' you " + "have to " + "write 'be innovative'.";
            case TECHNICAL ->
                    "You have to write in a technical style. This means you have to use precise language and avoid " + "using vague terms. For example, instead of writing 'a lot' you have to write 'many'. " + "Focus on technical terms and inovative solutions.";
        };
    }
}
