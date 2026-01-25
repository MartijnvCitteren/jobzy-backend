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


## Rules

✅ **Internal message**: Technical details, IDs, for debugging
✅ **Display message**: User-friendly, actionable, no technical jargon
✅ **Naming**: `[Resource][Condition]Exception` (e.g., `VacancyNotFoundException`)

❌ Never expose stack traces or sensitive data in `displayMessage`