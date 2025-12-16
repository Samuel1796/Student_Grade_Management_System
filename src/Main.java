import models.*;
import services.*;
import exceptions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

// Main application class for Student Grade Management System.
public class Main {
    public static void main(String[] args) {
        // Initialize services
        StudentService studentService = new StudentService();
        GradeService gradeService = new GradeService(500);
        MenuService menuService = new MenuService();

        // Initialize sample students and grades
        Collection<Student> students = studentService.getStudents();
        Grade[] grades = gradeService.getGrades();
        int[] studentCountRef = {studentService.getStudentCount()};
        int[] gradeCountRef = {gradeService.getGradeCount()};
//        StudentService.initializeSampleStudents(students, grades, studentCountRef, gradeCountRef);

        // Update the counts in the services
//        studentService.setStudentCount(studentCountRef[0]);
        gradeService.setGradeCount(gradeCountRef[0]);

        StatisticsService statisticsService = new StatisticsService(
                gradeService.getGrades(),
                gradeService.getGradeCount(),
                studentService.getStudents(),
                studentService.getStudentCount(),
                gradeService
        );

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        MainMenuHandler menuHandler = new MainMenuHandler(studentService, gradeService, menuService, statisticsService, sc);

        while (running) {
            menuService.displayMainMenu();
            int choice = sc.nextInt();
            sc.nextLine();
            running = menuHandler.handleMenu(choice);
        }
    }
}