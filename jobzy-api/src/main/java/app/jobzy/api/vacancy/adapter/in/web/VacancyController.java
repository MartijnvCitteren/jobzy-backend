package app.jobzy.api.vacancy.adapter.in.web;

import org.springframework.web.bind.annotation.RestController;

/**
 * No use case is wired in yet, so every operation falls through to {@link VacancyApi}'s default
 * methods, which respond with 501 Not Implemented.
 */
@RestController
public class VacancyController implements VacancyApi {}
