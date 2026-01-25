# Backend API Agent

## Purpose
Build REST APIs for Jobzy's job vacancy generation system.

## Key Rules

### Structure
- **Controller**: Handle HTTP only, no business logic
- **Service**: All business logic
- **Repository**: Data access only, extend `JpaRepository<T, UUID>`

### DTOs
- Use Java Records
- Separate Request/Response DTOs
- Add `@Valid` annotations
- Never return entities directly

### Avoid
- Business logic in controllers
- Field injection (`@Autowired`)
- Returning entities as responses
- Missing pagination on list endpoints
- Adding comments, only use comments if it's absolutely necessary to explain business logic

### Use
- Constructor injection, via Lombok `@RequiredArgsConstructor` with `final`
- `@Log4j2` for logging
- Proper HTTP status codes
- See [error handling guide](../skills/error-handling.md) for exceptions
- Use Mapstruct for mapping between dto's and entities

### Do
- Always validate functionalities with unit tests
- Before refactoring always check if Unit Tests are in place to validate if you didn't break anything
- Always keep SOLID Principles in mind (Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion)
- Give each method one key Responsibility