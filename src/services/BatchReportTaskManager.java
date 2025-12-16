package services;

import models.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchReportTaskManager {
    private final ExecutorService executor;
    private final List<Student> students;
    private final GradeService gradeService;
    private final int format;
    private final String outputDir;
    private final int totalTasks;
    private final int threadCount;
    private final ConcurrentHashMap<String, String> threadStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> reportTimes = new ConcurrentHashMap<>();
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);

    public BatchReportTaskManager(List<Student> students, GradeService gradeService, int format, String outputDir, int threadCount) {
        this.students = students;
        this.gradeService = gradeService;
        this.format = format;
        this.outputDir = outputDir;
        this.totalTasks = students.size();
        this.threadCount = threadCount;
        this.executor = Executors.newFixedThreadPool(threadCount);
        // Ensure output directory exists
        new java.io.File(outputDir).mkdirs();
    }

    public void startBatchExport() {
        long startTime = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();
        for (Student student : students) {
            futures.add(executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                threadStatus.put(student.getStudentID(), "in progress (" + threadName + ")");
                long t0 = System.currentTimeMillis();
                boolean success = false;
                try {
                    String filename = outputDir + student.getStudentID();
                    if (format == 2) { // Detailed Text
                        gradeService.exportGradeReport(student, 2, filename);
                        success = true;
                    } else if (format == 1) {
                        // TODO: PDF summary export
                    } else if (format == 3) {
                        // TODO: Excel export
                    } else if (format == 4) {
                        gradeService.exportGradeReportMultiFormat(student, 2, filename);
                        success = true;
                    }
                    threadStatus.put(student.getStudentID(), success ? "completed (" + threadName + ")" : "failed (" + threadName + ")");
                } catch (Exception e) {
                    threadStatus.put(student.getStudentID(), "failed (" + threadName + ")");
                    failedTasks.incrementAndGet();
                }
                long t1 = System.currentTimeMillis();
                reportTimes.put(student.getStudentID(), t1 - t0);
                completedTasks.incrementAndGet();
            }));
        }

        // Progress bar loop
        while (completedTasks.get() < totalTasks) {
            showProgress(startTime);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        showProgress(startTime); // Final update
        System.out.println("\nBATCH GENERATION COMPLETED!");
        showSummary(startTime);
        shutdown();
    }

    private void showProgress(long startTime) {
        int done = completedTasks.get();
        int active = threadCount - ((ThreadPoolExecutor) executor).getPoolSize();
        int queue = totalTasks - done - active;
        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        double avgTime = reportTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0) / 1000.0;
        double throughput = done / (elapsed > 0 ? elapsed : 1);

        System.out.printf("\rProgress: [%d/%d] | Elapsed: %.1fs | Avg Report Time: %.0fms | Throughput: %.2f reports/sec",
            done, totalTasks, elapsed, avgTime * 1000, throughput);

        // Per-thread status
        for (String id : threadStatus.keySet()) {
            System.out.printf("\n%s: %s", id, threadStatus.get(id));
        }
    }

    private void showSummary(long startTime) {
        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        int successCount = totalTasks - failedTasks.get();
        System.out.println("EXECUTION SUMMARY");
        System.out.println("Total Reports: " + totalTasks);
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + failedTasks.get());
        System.out.printf("Total Time: %.1f seconds\n", elapsed);
        System.out.printf("Avg Time per Report: %.0fms\n", reportTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0));
        System.out.println("Thread Pool Statistics:");
        System.out.println("Peak Thread Count: " + threadCount);
        System.out.println("Total Tasks Executed: " + totalTasks);
        System.out.println("Output Location: " + outputDir);
        System.out.println("Total Files Generated: " + successCount);
    }

    public int getActiveTasks() {
        int active = 0;
        for (String status : threadStatus.values()) {
            if (status.startsWith("in progress")) active++;
        }
        return active;
    }

    public boolean isRunning() {
        return completedTasks.get() < totalTasks;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}