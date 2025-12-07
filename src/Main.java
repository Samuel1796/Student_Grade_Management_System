import models.*;
import services.StudentService;
import services.GradeService;
import services.MenuService;

import java.util.Scanner;


//  Main application class for Student Grade Management System.
// Delegates operations to service classes.

public class Main {
    public static void main(String[] args) {
        StudentService studentService = new StudentService(50);
        GradeService gradeService = new GradeService(500);
        MenuService menuService = new MenuService();

        // Initialize sample students and grades
        Student[] students = studentService.getStudents();
        Grade[] grades = gradeService.getGrades();
        int[] studentCountRef = {studentService.getStudentCount()};
        int[] gradeCountRef = {gradeService.getGradeCount()};
        StudentService.initializeSampleStudents(students, grades, studentCountRef, gradeCountRef);

        // Update the counts in the services
        studentService.setStudentCount(studentCountRef[0]);
        gradeService.setGradeCount(gradeCountRef[0]);

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            menuService.displayMainMenu();
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    // Add Student
                    System.out.print("Enter student name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter student age: ");
                    int age = sc.nextInt();
                    sc.nextLine();
                    String email;
                    while (true) {
                        System.out.print("Enter student email: ");
                        email = sc.nextLine();
                        if (studentService.isValidEmail(email)) break;
                        System.out.println("Invalid email format. Try again.");
                    }
                    System.out.print("Enter student phone: ");
                    String phone = sc.nextLine();
                    if (studentService.isDuplicateStudent(name, email)) {
                        System.out.println("Duplicate student found.");
                        break;
                    }
                    System.out.println("Student type: ");
                    System.out.println("1. Regular Student (Passing grade: 50%)");
                    System.out.println("2. Honors Student (Passing grade: 60%, honors recognition)");
                    System.out.print("Select type (1-2): ");                    int type = sc.nextInt();
                    sc.nextLine();
                    Student newStudent = (type == 2)
                            ? new HonorsStudent(name, age, email, phone)
                            : new RegularStudent(name, age, email, phone);
                    if (studentService.addStudent(newStudent)) {
                        System.out.println("Student added successfully!");
                    } else {
                        System.out.println("Student database full!");
                    }
                    break;
                case 2:
                    // View Students
                    studentService.viewAllStudents(gradeService);
                    break;
                case 3:
                    System.out.println();
                    System.out.println("RECORD GRADE");
                    System.out.println("_________________________");
                
                    // GET STUDENT ID
                    System.out.print("Enter Student ID: ");
                    String studentID = sc.nextLine();
                    Student foundStudent = studentService.findStudentById(studentID);
                    double avg = foundStudent != null ? foundStudent.calculateAverage(gradeService) : 0;
                
                    if (foundStudent == null) {
                        System.out.println("Student not found!");
                        break;
                    }
                
                    System.out.println();
                    System.out.println("Student Details: ");
                    System.out.printf("Name: %s%n", foundStudent.getName());
                    System.out.printf("Type: %s%n", (foundStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student");
                    System.out.println();
                    System.out.printf("Current Average: %.1f%n", avg);
                    System.out.println();
                
                    // SELECT SUBJECT TYPE
                    System.out.println("Subject type:");
                    System.out.println("1. Core Subject (Mathematics, English, Science)");
                    System.out.println("2. Elective Subject (Art, Physical Education)");
                    System.out.println();
                
                    System.out.print("Select type (1-2): ");
                    int subjectType = sc.nextInt();
                    sc.nextLine();
                    System.out.println();
                
                    Subject subject = null;
                
                    if (subjectType == 1) {
                        System.out.println("Available Core Subject:");
                        System.out.println("1. Mathematics");
                        System.out.println("2. English");
                        System.out.println("3. Science");
                        System.out.print("Select subject (1-3): ");
                        int coreSubject = sc.nextInt();
                        sc.nextLine();
                        System.out.println();
                
                        switch (coreSubject) {
                            case 1: subject = new models.CoreSubject("Mathematics", "MATH101"); break;
                            case 2: subject = new models.CoreSubject("English", "ENG101"); break;
                            case 3: subject = new models.CoreSubject("Science", "SCI101"); break;
                            default:
                                System.out.println("Invalid choice");
                                break;
                        }
                    } else if (subjectType == 2) {
                        System.out.println("Available Elective Subject:");
                        System.out.println("1. Art");
                        System.out.println("2. Physical Education");
                        System.out.print("Select subject (1-2): ");
                        int electiveSubject = sc.nextInt();
                        sc.nextLine();
                        System.out.println();
                
                        switch (electiveSubject) {
                            case 1: subject = new models.ElectiveSubject("Art", "ART101"); break;
                            case 2: subject = new models.ElectiveSubject("Physical Education", "PE101"); break;
                            default:
                                System.out.println("Invalid choice");
                                break;
                        }
                    } else {
                        System.out.println("Invalid subject type selection!");
                        break;
                    }
                
                    if (subject == null) {
                        break;
                    }
                
                    // ENTER GRADE
                    System.out.print("Enter grade (0-100): ");
                    double gradeValue = sc.nextDouble();
                    sc.nextLine();
                
                    if (gradeValue < 0 || gradeValue > 100) {
                        System.out.println("Invalid grade, must be between 0 and 100");
                        break;
                    }
                    java.util.Date date = new java.util.Date();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                
                    // Generate Grade ID
                    String nextGradeID = String.format("GRD%03d", gradeService.getGradeCount() + 1);
                
                    // CONFIRMATION
                    System.out.println("CONFIRMATION:");
                    System.out.println("_________________________");
                    System.out.printf("Grade ID: %s%n", nextGradeID);
                    System.out.printf("Student: %s - %s%n", foundStudent.getStudentID(), foundStudent.getName());
                    System.out.printf("Subject: %s (%s)%n", subject.getSubjectName(), subject.getSubjectType());
                    System.out.printf("Grade: %.1f%n", gradeValue);
                    System.out.printf("Date: %s%n", sdf.format(date));
                    System.out.println("_________________________");
                    System.out.println();
                
                    System.out.print("Confirm? (Y/N): ");
                    String confirm = sc.nextLine();
                
                    if (confirm.equalsIgnoreCase("Y")) {
                        Grade grade = new Grade(nextGradeID, foundStudent.getStudentID(), subject.getSubjectName(), subject.getSubjectType(), gradeValue, date);
                        if (gradeService.recordGrade(grade)) {
                            System.out.printf("Grade recorded successfully! Grade ID: %s%n", nextGradeID);
                        } else {
                            System.out.println("Grade database full!");
                        }
                    } else {
                        System.out.println("Grade recording canceled.");
                    }
                    break;
                case 4:
                    // View Grade Report
                    System.out.print("Enter Student ID: ");
                    String idForReport = sc.nextLine();
                    Student studentForReport = studentService.findStudentById(idForReport);
                    if (studentForReport == null) {
                        System.out.println("Student not found!");
                        break;
                    }
                    gradeService.viewGradeReport(studentForReport);
                    break;
                case 5:
                    System.out.println("EXPORT GRADE REPORT");
                    System.out.println("_____________________________");

                    System.out.println();
                    System.out.print("Enter Student ID: ");
                    String exportStudentID = sc.nextLine();
                    Student exportStudent = studentService.findStudentById(exportStudentID);
                    if (exportStudent == null) {
                        System.out.println("Student not found!");
                        break;
                    }
                    System.out.printf("Student: %s - %s%n", exportStudent.getStudentID(), exportStudent.getName());
                    System.out.printf("Type: %s%n", (exportStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student");
                    System.out.println("Total Grades: " + gradeService.countGradesForStudent(exportStudent));
                    System.out.println("Export options:");
                    System.out.println("1. Summary Report (overview only)");
                    System.out.println("2. Detailed Report (all grades)");
                    System.out.println("3. Both");
                    int exportOption = 0;
                    while (exportOption < 1 || exportOption > 3) {
                        System.out.print("Select option (1-3): ");
                        try {
                            exportOption = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number between 1 and 3.");
                        }
                    }
                    System.out.print("Enter filename (without extension): ");
                    String filename = sc.nextLine().trim();
                    if (filename.isEmpty()) {
                        System.out.println("Filename cannot be empty.");
                        break;
                    }
                    try {
                        String exportPath = gradeService.exportGradeReport(exportStudent, exportOption, filename);
                        System.out.println("Report exported successfully!");
                        System.out.println("File: txt");
                        System.out.println("Location: ./reports/");
                        java.io.File file = new java.io.File(exportPath);
                        double sizeKB = file.length() / 1024.0;
                        System.out.printf("Size: %.1f KB%n", sizeKB);
                        System.out.printf("Contains: %d grades, averages, performance summary%n", gradeService.countGradesForStudent(exportStudent));
                    } catch (Exception e) {
                        System.out.println("Export failed: " + e.getMessage());
                    }
                    break;

                case 7:
                    System.out.println("BULK IMPORT GRADES");
                    System.out.println("_______________________________");
                    System.out.println("Place your CSV file in: ./imports/");
                    System.out.println("CSV Format Required:");
                    System.out.println("StudentID,SubjectName,SubjectType,Grade");
                    System.out.println("Example: STU001,Mathematics,Core,85");
                    System.out.print("Enter filename (without extension): ");
                    String importFilename = sc.nextLine().trim();
                    gradeService.bulkImportGrades(importFilename, studentService);
                break;

                case 10:
                    System.out.println("Thank you for using Student Grade Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, Try again ");
            }
            System.out.println();
        }
        sc.close();
    }
}