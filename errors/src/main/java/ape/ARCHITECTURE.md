# Errors Module Architecture

The errors module provides centralized error code definitions and annotations for the entire Adama platform. It serves as the single source of truth for all error codes used across modules.

## Key Concepts

- **Error Codes**: Unique integer identifiers for all error conditions
- **Annotations**: Metadata about error handling, retry behavior, and user visibility
- **Code Generation**: Automated table generation for documentation and lookups

## Package Structure

### Root Package (`ape`)

- **ErrorCodes.java**: Central listing of all error codes as static final integers. Each code is annotated with metadata indicating its behavior:
  - `@RetryInternally` - System should automatically retry this error
  - `@User` - Error should be shown to end users
  - `@Description` - Human-readable explanation of the error
  - `@NotProblem` - Expected condition, not an actual error
  - `@Group` - Categorization for documentation

- **ErrorTable.java**: Generated lookup table mapping error codes to their descriptions and metadata

- **GenerateTables.java**: Build-time tool that scans ErrorCodes and generates the ErrorTable

- **Description.java**: Annotation for adding human-readable error descriptions

- **Group.java**: Annotation for categorizing errors (e.g., "document", "auth", "network")

- **User.java**: Annotation marking errors that should be displayed to users

- **RetryInternally.java**: Annotation marking errors the system should auto-retry

- **NotProblem.java**: Annotation for expected conditions like "no change"

- **ManualUserTable.java**: Hand-curated user-facing error messages

## Error Code Ranges

Error codes are organized by subsystem:
- 100000-199999: Core runtime errors
- 200000-299999: Network errors
- 300000-399999: Storage errors
- 400000-499999: API errors

## Design Patterns

### Centralized Definition
```java
@User
@Description("The message given to the channel was not parsable")
public static final int LIVING_DOCUMENT_TRANSACTION_FAILED_PARSE_MESSAGE = 145627;
```

### Usage in Code
```java
throw new ErrorCodeException(ErrorCodes.DOCUMENT_NOT_FOUND);
```

## Key Files

| Function | File |
|----------|------|
| Error Definitions | `ErrorCodes.java` |
| Lookup Table | `ErrorTable.java` |
| Table Generator | `GenerateTables.java` |
| User Messages | `ManualUserTable.java` |
