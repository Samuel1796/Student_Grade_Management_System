import models.*;
import services.student.StudentService;
import services.file.GradeService;
import services.file.BatchReportTaskManager;
import services.menu.MenuService;
import services.menu.MainMenuHandler;
import services.analytics.StatisticsService;
import exceptions.*;
import java.io.IOException;
import java.util.*;

import utilities.FileIOUtils;
import utilities.Logger;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger.initialize();
        Logger.info("Application starting - Student Grade Management System");
        
        StudentService studentService = new StudentService();
        GradeService gradeService = new GradeService(500);
        MenuService menuService = new MenuService();

        // Initialize sample students and grades
        Collection<Student> students = studentService.getStudents();
        Grade[] grades = gradeService.getGrades();
        int[] studentCountRef = {studentService.getStudentCount()};
        int[] gradeCountRef = {gradeService.getGradeCount()};

        gradeService.setGradeCount(gradeCountRef[0]);

        StatisticsService statisticsService = new StatisticsService(
                gradeService.getGrades(),
                gradeService.getGradeCount(),
                studentService.getStudents(),
                studentService.getStudentCount(),
                gradeService
        );

        FileIOUtils.monitorDirectory(Paths.get("./imports"), () -> {
            System.out.println("New file detected in imports directory!");
            // Optionally trigger import logic here
        });

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        MainMenuHandler menuHandler = new MainMenuHandler(studentService, gradeService, menuService, statisticsService, sc);
        List<Student> studentList = new ArrayList<>(studentService.getStudents());
        int format = 1; // 1: CSV, 2: JSON, 3: Binary, 4: All formats
        String outputDir = "./reports/batch_2025-12-17/";
        int threadCount = 4;
        
        // Note: BatchReportTaskManager requires GradeImportExportService
        services.file.GradeImportExportService gradeImportExportService = new services.file.GradeImportExportService(gradeService);
        BatchReportTaskManager batchManager = new BatchReportTaskManager(studentList, gradeImportExportService, format, outputDir, threadCount);

        while (running) {
            menuService.displayMainMenu();

            int choice = sc.nextInt();
            sc.nextLine();
            running = menuHandler.handleMenu(choice);
        }
    }
}