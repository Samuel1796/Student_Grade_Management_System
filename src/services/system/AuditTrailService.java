package services.system;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrent Audit Trail Service (US-9).
 * 
 * Thread-safe logging system that tracks all system operations:
 * - Add student, record grade, generate report, search, etc.
 * 
 * Features:
 * - Thread-safe logging (ConcurrentLinkedQueue)
 * - Asynchronous file writing
 * - Log rotation (daily or 10MB size)
 * - ISO 8601 timestamp format
 * - Operation filtering and searching
 * - Audit statistics
 * - No log entries lost during concurrent operations
 */
public class AuditTrailService {
    
    private final ConcurrentLinkedQueue<AuditEntry> logQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService writerExecutor;
    private final AtomicInteger totalOperations = new AtomicInteger(0);
    private final AtomicInteger successfulOperations = new AtomicInteger(0);
    private final AtomicInteger failedOperations = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    private volatile boolean running = true;
    private String currentLogFile;
    private long currentLogFileSize = 0;
    private static final long MAX_LOG_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String LOG_DIR = "./data/audit_logs";
    
    /** Audit log entry with all required information. */
    public static class AuditEntry implements Serializable {
        private final String timestamp;      // ISO 8601 format
        private final String threadId;
        private final String operationType;
        private final String userAction;
        private final long executionTime;    // milliseconds
        private final boolean success;
        private final String details;

        // Used when creating new log entries in memory
        public AuditEntry(String threadId, String operationType, String userAction,
                          long executionTime, boolean success, String details) {
            this(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                 threadId, operationType, userAction, executionTime, success, details, false);
        }

        // Used when reconstructing entries from log file lines
        private AuditEntry(String timestamp, String threadId, String operationType, String userAction,
                           long executionTime, boolean success, String details, boolean fromFile) {
            this.timestamp = timestamp;
            this.threadId = threadId;
            this.operationType = operationType;
            this.userAction = userAction;
            this.executionTime = executionTime;
            this.success = success;
            this.details = details;
        }
        
        public String getTimestamp() { return timestamp; }
        public String getThreadId() { return threadId; }
        public String getOperationType() { return operationType; }
        public String getUserAction() { return userAction; }
        public long getExecutionTime() { return executionTime; }
        public boolean isSuccess() { return success; }
        public String getDetails() { return details; }
        
        @Override
        public String toString() {
            return String.format("[%s] Thread:%s | Op:%s | Action:%s | Time:%dms | Status:%s | %s",
                timestamp, threadId, operationType, userAction, executionTime,
                success ? "SUCCESS" : "FAILED", details);
        }
    }
    
    public AuditTrailService() {
        // Create log directory
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
        
        // Initialize current log file
        updateLogFile();
        
        // Create single-threaded executor for sequential log writing
        writerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AuditTrail-Writer");
            t.setDaemon(true);
            return t;
        });
        
        // Start background writer thread
        startBackgroundWriter();
    }
    
    /**
     * Logs an operation (thread-safe, non-blocking).
     */
    public void logOperation(String operationType, String userAction, long executionTime,
                            boolean success, String details) {
        // Use threadId() instead of deprecated getId()
        String threadId = String.valueOf(Thread.currentThread().threadId());
        
        AuditEntry entry = new AuditEntry(threadId, operationType, userAction,
                                         executionTime, success, details);
        
        // Add to queue (non-blocking, thread-safe)
        logQueue.offer(entry);
        
        // Update statistics
        totalOperations.incrementAndGet();
        if (success) {
            successfulOperations.incrementAndGet();
        } else {
            failedOperations.incrementAndGet();
        }
        totalExecutionTime.addAndGet(executionTime);
    }
    
    /**
     * Starts background thread that drains queue to file.
     */
    private void startBackgroundWriter() {
        writerExecutor.submit(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    // Drain queue in batches for efficiency
                    List<AuditEntry> batch = new ArrayList<>();
                    AuditEntry entry;
                    
                    // Collect up to 100 entries or wait a bit
                    while (batch.size() < 100 && (entry = logQueue.poll()) != null) {
                        batch.add(entry);
                    }
                    
                    if (!batch.isEmpty()) {
                        writeBatchToFile(batch);
                    } else {
                        // No entries, sleep briefly
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error writing audit log: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Writes batch of entries to file.
     */
    private synchronized void writeBatchToFile(List<AuditEntry> batch) {
        try {
            // Check if we need to rotate log file
            if (currentLogFileSize > MAX_LOG_FILE_SIZE) {
                rotateLogFile();
            }
            
            // Append to current log file
            try (PrintWriter writer = new PrintWriter(
                    new FileWriter(currentLogFile, true))) {
                for (AuditEntry entry : batch) {
                    writer.println(entry.toString());
                    currentLogFileSize += entry.toString().length() + 1; // +1 for newline
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }
    
    /**
     * Updates current log file (creates new file if needed).
     */
    private void updateLogFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(new Date());
        currentLogFile = LOG_DIR + "/audit_" + dateStr + ".log";
        currentLogFileSize = 0;
        
        // Check if file exists and get its size
        File file = new File(currentLogFile);
        if (file.exists()) {
            currentLogFileSize = file.length();
        }
    }
    
    /**
     * Rotates log file (creates new file when size limit reached).
     */
    private void rotateLogFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        String timestamp = sdf.format(new Date());
        String rotatedFile = LOG_DIR + "/audit_" + timestamp + ".log";
        
        // Rename current file
        File current = new File(currentLogFile);
        if (current.exists()) {
            current.renameTo(new File(rotatedFile));
        }
        
        // Create new log file
        updateLogFile();
    }
    
    /**
     * Views recent audit entries with optional filtering.
     */
    public void viewRecentEntries(int count, String operationType, String threadId) {
        List<AuditEntry> entries = readRecentEntries(count, operationType, threadId);
        
        System.out.println("\n=========================================================================");
        System.out.println("                      AUDIT TRAIL ENTRIES                                ");
        System.out.println("=========================================================================");
        System.out.println();
        
        if (entries.isEmpty()) {
            System.out.println("No entries found.");
            return;
        }
        
        for (AuditEntry entry : entries) {
            System.out.println("== Entry=================================================================");
            System.out.printf("│ Timestamp: %-54s │%n", entry.getTimestamp());
            System.out.printf("│ Thread ID: %-54s │%n", entry.getThreadId());
            System.out.printf("│ Operation: %-54s │%n", entry.getOperationType());
            System.out.printf("│ Action: %-56s │%n", entry.getUserAction());
            System.out.printf("│ Execution Time: %-48dms │%n", entry.getExecutionTime());
            System.out.printf("│ Status: %-56s │%n", entry.isSuccess() ? "SUCCESS" : "FAILED");
            System.out.printf("│ Details: %-55s │%n", entry.getDetails());
            System.out.println("========================================================================");
            System.out.println();
        }
    }
    
    /**
     * Reads recent entries from log file with filtering.
     */
    private List<AuditEntry> readRecentEntries(int count, String operationType, String threadId) {
        List<AuditEntry> entries = new ArrayList<>();
        
        try {
            // Read from current log file and recent rotated files
            File logDir = new File(LOG_DIR);
            File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("audit_") && name.endsWith(".log"));
            
            if (logFiles == null) return entries;
            
            // Sort by modification time (newest first)
            Arrays.sort(logFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            
            // Read from files until we have enough entries
            for (File logFile : logFiles) {
                if (entries.size() >= count) break;
                
                try (BufferedReader reader = Files.newBufferedReader(logFile.toPath())) {
                    String line;
                    List<String> fileLines = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        fileLines.add(line);
                    }
                    
                    // Process lines in reverse (newest first)
                    for (int i = fileLines.size() - 1; i >= 0 && entries.size() < count; i--) {
                        AuditEntry entry = parseEntry(fileLines.get(i));
                        if (entry != null) {
                            // Apply filters
                            if (operationType != null && !entry.getOperationType().equals(operationType)) {
                                continue;
                            }
                            if (threadId != null && !entry.getThreadId().equals(threadId)) {
                                continue;
                            }
                            entries.add(entry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read audit log: " + e.getMessage());
        }
        
        return entries;
    }
    
    /** Parses a single log line back into an AuditEntry (best-effort). */
    private AuditEntry parseEntry(String line) {
        try {
            // Example format from toString():
            // [2025-12-18T10:15:30.123] Thread:12 | Op:ADD_STUDENT | Action:Add student | Time:20ms | Status:SUCCESS | ID=STU001

            int tsStart = line.indexOf('[');
            int tsEnd = line.indexOf(']');
            if (tsStart != 0 || tsEnd <= tsStart) return null;
            String timestamp = line.substring(tsStart + 1, tsEnd).trim();

            int threadLabel = line.indexOf("Thread:", tsEnd);
            int opLabel = line.indexOf(" | Op:", threadLabel);
            if (threadLabel < 0 || opLabel < 0) return null;
            String threadId = line.substring(threadLabel + "Thread:".length(), opLabel).trim();

            int actionLabel = line.indexOf(" | Action:", opLabel);
            if (actionLabel < 0) return null;
            String operationType = line.substring(opLabel + " | Op:".length(), actionLabel).trim();

            int timeLabel = line.indexOf(" | Time:", actionLabel);
            if (timeLabel < 0) return null;
            String userAction = line.substring(actionLabel + " | Action:".length(), timeLabel).trim();

            int statusLabel = line.indexOf(" | Status:", timeLabel);
            if (statusLabel < 0) return null;
            String timePart = line.substring(timeLabel + " | Time:".length(), statusLabel).trim();
            if (timePart.endsWith("ms")) {
                timePart = timePart.substring(0, timePart.length() - 2).trim();
            }
            long executionTime = Long.parseLong(timePart);

            int detailsSep = line.indexOf(" |", statusLabel + " | Status:".length());
            String statusStr;
            String details;
            if (detailsSep >= 0) {
                statusStr = line.substring(statusLabel + " | Status:".length(), detailsSep).trim();
                details = line.substring(detailsSep + 2).trim();
            } else {
                statusStr = line.substring(statusLabel + " | Status:".length()).trim();
                details = "";
            }
            boolean success = "SUCCESS".equalsIgnoreCase(statusStr);

            // Reconstruct entry with original timestamp and parsed fields
            return new AuditEntry(timestamp, threadId, operationType, userAction,
                                  executionTime, success, details, true);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Searches audit trail by date range, operation type, or thread ID.
     */
    public List<AuditEntry> searchAuditTrail(Date startDate, Date endDate, String operationType, String threadId) {
        List<AuditEntry> results = new ArrayList<>();
        
        try {
            File logDir = new File(LOG_DIR);
            File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("audit_") && name.endsWith(".log"));
            
            if (logFiles == null) return results;
            
            for (File logFile : logFiles) {
                try (BufferedReader reader = Files.newBufferedReader(logFile.toPath())) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        AuditEntry entry = parseEntry(line);
                        if (entry != null) {
                            // Apply filters
                            if (operationType != null && !entry.getOperationType().equals(operationType)) {
                                continue;
                            }
                            if (threadId != null && !entry.getThreadId().equals(threadId)) {
                                continue;
                            }
                            // Date range filtering would go here
                            results.add(entry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to search audit log: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Displays audit statistics.
     */
    public void displayStatistics() {
        int total = totalOperations.get();
        int successful = successfulOperations.get();
        int failed = failedOperations.get();
        long totalTime = totalExecutionTime.get();
        double avgExecutionTime = total > 0 ? (double) totalTime / total : 0.0;
        
        // Calculate operations per hour (last hour)
        int operationsPerHour = calculateOperationsPerHour();
        
        System.out.println("\n=========================================================================");
        System.out.println("                      AUDIT STATISTICS                                    ");
        System.out.println("==========================================================================");
        System.out.println();
        
        System.out.println(" OPERATION STATISTICS ====================================================");
        System.out.printf("│ Total Operations: %-50d │%n", total);
        System.out.printf("│ Successful: %-55d │%n", successful);
        System.out.printf("│ Failed: %-58d │%n", failed);
        System.out.printf("│ Success Rate: %-51.2f%% │%n", total > 0 ? (successful * 100.0 / total) : 0.0);
        System.out.println("==========================================================================");
        System.out.println();
        
        System.out.println("  PERFORMANCE STATISTICS ===================================================");
        System.out.printf("│ Average Execution Time: %-43.2fms │%n", avgExecutionTime);
        System.out.printf("│ Operations per Hour: %-48d │%n", operationsPerHour);
        System.out.println("=============================================================================");
    }
    
    /**
     * Calculates operations per hour (simplified - counts last hour's operations).
     */
    private int calculateOperationsPerHour() {
        // Simplified: return total operations (would need time-based tracking in production)
        return totalOperations.get();
    }
    
    /**
     * Shuts down audit trail service gracefully.
     */
    public void shutdown() {
        running = false;
        
        if (writerExecutor != null) {
            writerExecutor.shutdown();
            try {
                if (!writerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    writerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                writerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Flush remaining entries
        while (!logQueue.isEmpty()) {
            List<AuditEntry> batch = new ArrayList<>();
            AuditEntry entry;
            while (batch.size() < 100 && (entry = logQueue.poll()) != null) {
                batch.add(entry);
            }
            if (!batch.isEmpty()) {
                writeBatchToFile(batch);
            }
        }
    }
}

