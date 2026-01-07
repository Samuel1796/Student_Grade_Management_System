# Logging Standards

This document defines the consistent logging patterns used throughout the Student Grade Management System.

## Logging Architecture

The system uses a dual-layer logging approach:

1. **Logger Utility** (`utilities.Logger`) - Centralized logging with:
   - In-memory log storage (up to 10,000 entries)
   - Daily log file rotation (`logs/app-YYYY-MM-DD.log`)
   - Console and file output
   - Thread-safe concurrent logging

2. **AuditTrailService** (`services.system.AuditTrailService`) - Specialized audit logging with:
   - Asynchronous file writing via background thread
   - Thread-safe queue-based logging
   - Real-time log viewing from memory and files
   - Operation tracking and statistics

## Log Message Format

### Standard Log Format

All log messages are formatted as:
```
[YYYY-MM-DD HH:mm:ss.SSS] [LEVEL] [THREAD_NAME] MESSAGE
```

Example:
```
[2025-12-17 10:30:45.123] [INFO] [main] APPLICATION: Starting - Student Grade Management System
```

### Operation Name Pattern

All log messages follow the pattern: `OPERATION_NAME: Message`

### Operation Names
- `APPLICATION` - Application lifecycle events (start, shutdown, exit)
- `ADD_STUDENT` - Student addition operations
- `FIND_STUDENT` - Student lookup operations
- `VIEW_STUDENTS` - Viewing all students
- `RECORD_GRADE` - Grade recording operations
- `UPDATE_GRADE` - Grade update operations
- `VIEW_GRADE_REPORT` - Viewing individual grade reports
- `CALCULATE_GPA` - GPA calculation operations
- `CALCULATE_STUDENT_GPA` - Individual student GPA calculation
- `VIEW_STATISTICS` - Class statistics viewing
- `VIEW_CLASS_STATISTICS` - Class statistics operations
- `REAL_TIME_STATISTICS_DASHBOARD` - Real-time dashboard operations
- `EXPORT_GRADE_REPORT` - Grade report export operations
- `IMPORT_STUDENT` - Student import operations
- `BULK_IMPORT_GRADES` - Bulk grade import operations
- `GENERATE_BATCH_REPORTS` - Batch report generation
- `SEARCH_STUDENTS` - Student search operations
- `PATTERN_BASED_SEARCH` - Pattern-based search operations
- `BATCH_EXPORT` - Individual batch export operations
- `BATCH_EXPORT_ALL` - Complete batch export operations
- `SCHEDULE_TASK` - Task scheduling operations
- `SCHEDULED_TASK` - Scheduled task execution
- `SCHEDULED_TASK_EXECUTION` - Scheduled task performance metrics
- `TASK_SCHEDULER` - Task scheduler initialization
- `BATCH_REPORT_MANAGER` - Batch report manager initialization
- `MENU_HANDLER` - Menu handler errors
- `VALIDATION_UTILS` - Input validation errors

## Log Levels

### INFO
- Operation start: `OPERATION_NAME: Starting`
- Operation completion: `OPERATION_NAME: Completed`
- Initialization: `COMPONENT_NAME: Initialized with details`
- General information: `OPERATION_NAME: Information message`

### DEBUG
- Detailed operation information: `OPERATION_NAME: Detailed message`
- Internal state changes: `OPERATION_NAME: State change description`

### WARN
- Non-critical errors: `OPERATION_NAME: Warning description - details`
- Validation failures: `OPERATION_NAME: Validation failed - reason`
- Retryable errors: `OPERATION_NAME: Retryable error - details`

### ERROR
- Critical errors: `OPERATION_NAME: Error description - details`
- Always include exception: `Logger.error("OPERATION_NAME: Error - " + e.getMessage(), e)`

## Audit Trail Logging

### Using Logger.logAudit()

Format: `Logger.logAudit(operation, action, duration, success, details)`

- **operation**: Operation name (e.g., "ADD_STUDENT")
- **action**: Brief action description (e.g., "Add student STU001")
- **duration**: Execution time in milliseconds
- **success**: true/false
- **details**: Additional details or error message

The log message is formatted as:
```
AUDIT: OPERATION | Action: ACTION | Time: DURATION ms | Status: SUCCESS/FAILED | DETAILS
```

Example:
```java
Logger.logAudit("ADD_STUDENT", "Add student STU001", duration, true, "Student added successfully");
// Output: AUDIT: ADD_STUDENT | Action: Add student STU001 | Time: 5 ms | Status: SUCCESS | Student added successfully

Logger.logAudit("ADD_STUDENT", "Add student STU001", duration, false, "Duplicate student ID: STU001");
// Output: AUDIT: ADD_STUDENT | Action: Add student STU001 | Time: 2 ms | Status: FAILED | Duplicate student ID: STU001
```

### Using AuditTrailService.logOperation()

For more advanced audit tracking, use `AuditTrailService`:

Format: `auditTrailService.logOperation(operationType, userAction, executionTime, success, details)`

This method:
- Adds entry to thread-safe queue
- Writes asynchronously to log files via background thread
- Tracks operation statistics (total, successful, failed)
- Maintains execution time metrics

Example:
```java
long startTime = System.currentTimeMillis();
// ... perform operation ...
long executionTime = System.currentTimeMillis() - startTime;
auditTrailService.logOperation("ADD_STUDENT", "Added student STU001", executionTime, true, "Student: John Doe");
```

### Viewing Audit Logs

Use `AuditTrailService.displayAuditTrailViewer()` to view audit logs:
- Reads from in-memory logs (most up-to-date)
- Falls back to log files if memory is empty
- Shows up to 50 most recent entries
- Displays pending queue entries

The viewer automatically:
- Checks `Logger.getAllLogs()` for in-memory entries
- Filters for entries containing "AUDIT:"
- Reads from log files as backup
- Shows helpful messages if no logs exist

## Performance Logging

Format: `Logger.logPerformance(operation, duration, metrics)`

- **operation**: Operation name
- **duration**: Execution time in milliseconds
- **metrics**: Map containing relevant metrics (collection sizes, thread pool stats, etc.)

Example:
```java
Map<String, Object> metrics = new HashMap<>();
metrics.put("studentCount", studentMap.size());
Logger.logPerformance("ADD_STUDENT", duration, metrics);
```

## Log File Management

### Log File Location
- **Directory**: `logs/` (relative to application root)
- **Naming**: `app-YYYY-MM-DD.log` (daily rotation)
- **Format**: Plain text with timestamped entries

### Log Rotation
- Logs are automatically rotated daily at midnight
- Old log files are preserved (not deleted)
- Each day gets a new log file: `app-2025-12-17.log`, `app-2025-12-18.log`, etc.

### In-Memory Logs
- Up to 10,000 log entries stored in memory
- Fast access via `Logger.getAllLogs()`, `Logger.getRecentLogs(count)`, `Logger.getLogsByLevel(level)`
- Automatically pruned when limit exceeded (FIFO)

### Log Export
- Use `Logger.exportLogsToFile()` to export all in-memory logs
- Creates timestamped export file: `exported_logs_YYYY-MM-DD_HHmmss.txt`
- Includes statistics and all log entries

## Best Practices

1. **Always use operation name prefix** - Makes logs searchable and filterable
2. **Include context in messages** - Add relevant IDs, counts, or identifiers
3. **Log at appropriate levels** - Use INFO for normal flow, WARN for recoverable issues, ERROR for failures
4. **Include exceptions** - Always pass exception object to error() method: `Logger.error("MESSAGE", exception)`
5. **Log operation start and end** - Helps track operation lifecycle
6. **Use consistent action descriptions** - Keep audit trail action descriptions concise and consistent
7. **Include metrics for performance** - Log collection sizes, thread pool stats, etc. for performance monitoring
8. **Use AuditTrailService for user actions** - All user-initiated operations should be logged via `auditTrailService.logOperation()`
9. **Measure execution time** - Always track operation duration for audit logs
10. **Flush logs on shutdown** - Logger automatically exports logs on shutdown via shutdown hook

## Examples

### Operation Start
```java
Logger.info("CALCULATE_STUDENT_GPA: Starting");
```

### Success Audit
```java
Logger.logAudit("CALCULATE_STUDENT_GPA", "Calculate GPA for STU001", duration, true, "GPA calculated successfully");
```

### Failure Audit
```java
Logger.logAudit("CALCULATE_STUDENT_GPA", "Calculate GPA", duration, false, "Student not found: STU001");
```

### Error with Exception
```java
Logger.error("ADD_STUDENT: Error adding student - " + e.getMessage(), e);
```

### Warning
```java
Logger.warn("ADD_STUDENT: Duplicate student detected - " + e.getMessage());
```

### Performance
```java
Map<String, Object> metrics = new HashMap<>();
metrics.put("resultCount", searchResults.length);
metrics.put("searchOption", searchOption);
Logger.logPerformance("SEARCH_STUDENTS", duration, metrics);
// Output: PERF: SEARCH_STUDENTS took 15 ms | Metrics: resultCount=5 searchOption=name
```

### Using AuditTrailService
```java
// In MainMenuHandler or service classes
long startTime = System.currentTimeMillis();
// ... perform operation ...
long executionTime = System.currentTimeMillis() - startTime;
auditTrailService.logOperation("VIEW_STATISTICS", "Viewed class statistics", executionTime, true, 
    "Students: " + studentCount + ", Grades: " + gradeCount);
```

### Accessing Logs Programmatically
```java
// Get all logs from memory
List<Logger.LogEntry> allLogs = Logger.getAllLogs();

// Get recent logs
List<Logger.LogEntry> recentLogs = Logger.getRecentLogs(50);

// Get logs by level
List<Logger.LogEntry> errorLogs = Logger.getLogsByLevel("ERROR");

// Get logger statistics
Map<String, Object> stats = Logger.getStatistics();
// Returns: totalLogs, errorCount, warnCount, currentLogFile, averageLogTimeNs

// Get log directory
String logDir = Logger.getLogDirectory(); // Returns "logs"

// Get current log file path
String logFile = Logger.getCurrentLogFile(); // Returns "logs/app-2025-12-17.log"
```

## Thread Safety

All logging operations are thread-safe:
- `Logger` uses `ConcurrentLinkedQueue` for in-memory logs
- `AuditTrailService` uses `ConcurrentLinkedQueue` for audit entries
- File writing is synchronized to prevent corruption
- Background threads handle asynchronous log writing

## Log Levels Reference

| Level | Usage | Example |
|-------|-------|---------|
| **DEBUG** | Detailed information | `Logger.debug("Processing student: " + studentId)");` |
| **INFO** | Normal operation flow | `Logger.info("APPLICATION: Starting - Student Grade Management System");` |
| **WARN** | Recoverable issues | `Logger.warn("ADD_STUDENT: Duplicate student detected - " + e.getMessage());` |
| **ERROR** | Critical failures | `Logger.error("ADD_STUDENT: Error adding student - " + e.getMessage(), e);` |

## Audit Trail Format

Audit log entries in files appear as:
```
[2025-12-17 10:30:45.123] [INFO] [main] AUDIT: ADD_STUDENT | Action: Add student STU001 | Time: 5 ms | Status: SUCCESS | Student added successfully
```

When parsed by `AuditTrailService`, entries are converted to structured `AuditEntry` objects with:
- Timestamp (ISO 8601 format)
- Thread ID
- Operation Type
- User Action
- Execution Time (milliseconds)
- Success Status
- Details

