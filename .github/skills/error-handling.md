# Error Handling Skill

## Pattern

All exceptions extend `BaseException` and know their HTTP status:
```java
public class VacancyNotFoundException extends BaseException {
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String DISPLAY_MESSAGE = "The vacancy you're looking for doesn't exist";

    public VacancyNotFoundException(UUID vacancyId) {
        super(STATUS, "Vacancy not found: " + vacancyId, DISPLAY_MESSAGE);
    }
}
```

## Response Format

Always return `ErrorDto`:
```java
{
  "status": "404",
  "error": "Not Found",
  "message": "Vacancy not found: 123e4567...",  // Technical, for logs
  "displayMessage": "The vacancy you're looking for doesn't exist"  // User-friendly
}
```

## HTTP Status Codes

Use standard HTTP statuses that match the exception semantics:

- `400 Bad Request` – Validation failures, malformed input.
- `401 Unauthorized` – Missing or invalid authentication.
- `403 Forbidden` – Authenticated, but not allowed to access the resource.
- `404 Not Found` – Resource does not exist (e.g., `VacancyNotFoundException`).
- `409 Conflict` – State conflicts (e.g., duplicate resource, illegal state transition).
- `422 Unprocessable Entity` – Business rule violations on otherwise valid input.
- `500 Internal Server Error` – Unexpected errors not mapped to a specific status.
## Rules

✅ **Internal message**: Technical details, IDs, for debugging
✅ **Display message**: User-friendly, actionable, no technical jargon
✅ **Naming**: `[Resource][Condition]Exception` (e.g., `VacancyNotFoundException`)

❌ Never expose stack traces or sensitive data in `displayMessage`