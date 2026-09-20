package app.jobzy.api.application.port.in.vacancy.command;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GenerateVacancyDescriptionCommand(
    UUID vacancyId, String tasks, String team, String niceAboutJob) {}
