// java
// Main application class handling student grade management
//  Contains menu system and core business logic
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    // Array to store student records (max 50 students)
    private static final Student[] students  = new Student[50];
    private static int studentCount = 0;

    //storage for grade records
    private static final Grade[] grades = new Grade[500];
    private static int gradeCount = 0;

//         Validates email format using regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    private static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }


    public static void main(String[] args) {

        initializeSampleStudents();

        System.out.println("|===================================================|");
        System.out.println("|        STUDENT GRADE MANAGEMENT - MAIN MENU       |");
        System.out.println("|===================================================|");

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            displayMenu();
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addStudent(sc);
                    break;
                case 2:
                    viewStudents();
                    break;
                case 3:
                    recordGrade(sc);
                    break;
                case 4:
                    viewGradeReport(sc);
                    break;
                case 5:
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

//    Handles the main menu
    public static void displayMenu() {
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Exit");
        System.out.println();
    }



//      Finds a student by ID (case insensitive): Takes studentID as input
//      and returns the Student object if found, otherwise null.
    private static Student findStudentById(String studentID) {
        for (int i = 0; i < studentCount; i++) {
            String currentId = students[i].getStudentID();
            if (currentId != null && currentId.equalsIgnoreCase(studentID)) {
                return students[i];
            }
        }
        return null;
    }
//    Check for duplicates before adding a new student
//
    private static boolean isDuplicateStudent(String name, String email) {
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            if (s.getName().equalsIgnoreCase(name) && s.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }


//          Adds a new student to the system
    public static void addStudent(Scanner sc){
        System.out.println();
        System.out.println("ADD STUDENT");
        System.out.println("_________________________");

        if (studentCount >= students.length) {
            System.out.println("ERROR: Student database is full!");
            return;
        }

        System.out.println();
        System.out.print("Enter student name: ");
        String name = sc.nextLine();

        System.out.print("Enter student age: ");
        int age = sc.nextInt();
        sc.nextLine();

        String email;
        while (true) {
            System.out.print("Enter student mail: ");
            email = sc.nextLine();
            if (isValidEmail(email)) {
                break;
            }
            System.out.println("Invalid email format. Please try again.");
        }

        System.out.print("Enter student phone: ");
        String phone = sc.nextLine();
        System.out.println();

        // Check for duplicates before proceeding
        if (isDuplicateStudent(name, email)) {
            System.out.println("A student with this name and email already exists");
            return;
        }

        System.out.println("Student type: ");
        System.out.println("1. Regular Student (Passing grade: 50%)");
        System.out.println("2. Honors Student (Passing grade: 60%, honors recognition)");
        System.out.print("Select type (1-2): ");
        int type = sc.nextInt();
        sc.nextLine();
        System.out.println();

        Student newStudent;

        if (type == 1){
            newStudent = new RegularStudent(name, age, email, phone);
        }else if(type == 2){
            newStudent = new HonorsStudent(name, age, email, phone);
        }else{
            System.out.println("Invalid type, Creating Regular Student by default");
            newStudent = new RegularStudent(name, age, email, phone);
        }

        students[studentCount] = newStudent;
        studentCount++;

//        Get the type of student(at runtime for display)
        String typeName = (newStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student";


        System.out.println("Student added successfully!");
        System.out.println("_______________________________");

        System.out.printf("Student ID: %s%n", newStudent.getStudentID());
        System.out.printf("Name: %s%n", newStudent.getName());
        System.out.printf("Type: %s%n", typeName);
        System.out.printf("Age: %d%n", newStudent.getAge());
        System.out.printf("Email: %s%n", newStudent.getEmail());
        System.out.printf("Passing Grade: %d%n", newStudent.getPassingGrade());
        System.out.println(newStudent.honorsEligible ? "Honors Eligible: Yes" : "");
        System.out.printf("Status: %s%n", newStudent.getStatus());
        System.out.println("_______________________________");
    }


//          Displays all students in a formatted table
    public static void viewStudents(){
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        System.out.println();
        System.out.println("STUDENT LISTING");
        System.out.println("________________________________________________________________________________________________________________________________________________");
        System.out.println("| STUDENT ID  | NAME                  | TYPE            | AVG GRADE | STATUS       | ENROLLED SUBJECTS    | PASSING GRADE | Honors Eligible     |");
        System.out.println("|_______________________________________________________________________________________________________________________________________________|");
        for (int i = 0; i < studentCount; i++) {
            Student student = students[i];

            double avg = student.calculateAverage();


            System.out.printf("| %-10s | %-20s | %-15s | %-9.1f | %-12s | %-20s | %-13d | %-18s |%n",
                    student.getStudentID(),
                    student.getName(),
                    (student instanceof HonorsStudent) ? "Honors" : "Regular",
                    avg,
                    student.getStatus(),
                    student.getEnrolledSubjectsString().length(),
                    student.getPassingGrade(),
                    student.isHonorsEligible() ? "Yes" : "No");
        }
        System.out.println("|=============================================================================================================================================|");
    }


//         Records a new grade for a student in a specified subject
    public static void recordGrade(Scanner sc) {

        System.out.println();
        System.out.println("RECORD GRADE");
        System.out.println("_________________________");

//        GET STUDENT ID
        System.out.print("Enter Student ID: ");
        String studentID = sc.nextLine();
        Student foundStudent = findStudentById(studentID);
        double avg = foundStudent != null ? foundStudent.calculateAverage() : 0;



        if (foundStudent == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.println();
        System.out.println("Student Details: ");
        System.out.printf("Name: %s%n", foundStudent.getName());
        System.out.printf("Type: %s%n", (foundStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student");
        System.out.println();
        System.out.printf("Current Average: %f",avg);
        System.out.println();



//        SELECT SUBJECT TYPE
        System.out.println("Subject type:");
        System.out.println("1. Core Subject (Mathematics, English, Science)");
        System.out.println("2. Elective Subject (Art, Physical Education)");
        System.out.println();

        System.out.print("Select type (1-2): ");
        int type = sc.nextInt();
        sc.nextLine();
        System.out.println();

        Subject subject;

        if (type == 1) {
            System.out.println("Available Core Subject:");
            System.out.println("1. Mathematics");
            System.out.println("2. English");
            System.out.println("3. Science");
            System.out.print("Select subject (1-3): ");
            int coreSubject = sc.nextInt();
            sc.nextLine();
            System.out.println();

            switch (coreSubject) {
                case 1: subject = new CoreSubject("Mathematics", "MATH101"); break;
                case 2: subject = new CoreSubject("English", "ENG101"); break;
                case 3: subject = new CoreSubject("Science", "SCI101"); break;
                default:
                    System.out.println("Invalid choice");
                    return;
            }
        } else if (type == 2) {
            System.out.println("Available Elective Subject:");
            System.out.println("1. Art");
            System.out.println("2. Physical Education");
            System.out.print("Select subject (1-2): ");
            int electiveSubject = sc.nextInt();
            sc.nextLine();
            System.out.println();

            switch (electiveSubject) {
                case 1: subject = new ElectiveSubject("Art", "ART101"); break;
                case 2: subject = new ElectiveSubject("Physical Education", "PE101"); break;
                default:
                    System.out.println("Invalid choice");
                    return;
            }
        } else {
            System.out.println("Invalid subject type selection!");
            return;
        }

//        ENTER GRADE

        System.out.print("Enter grade (0-100): ");
        double grade = sc.nextDouble();
        sc.nextLine();

        if (grade < 0 || grade > 100) {
            System.out.println("Invalid grade, must be between 0 and 100");
            return;
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");


//        Generate Grade ID
        String nextGradeID = String.format("GRD%03d", gradeCount + 1);



//        CONFIRMATION
        System.out.println("CONFIRMATION:");
        System.out.println("_________________________");
        System.out.printf("Grade ID: %s%n", nextGradeID);
        System.out.printf("Student: %s - %s%n", foundStudent.getStudentID(), foundStudent.getName());
        System.out.printf("Subject: %s (%s)%n", subject.getSubjectName(), subject.getSubjectType());
        System.out.printf("Grade: %.1f%n", grade);
        System.out.printf("Date: %s%n", sdf.format(date));
        System.out.println("_________________________");
        System.out.println();



        System.out.print("Confirm? (Y/N): ");
        String confirm = sc.nextLine();


        if (confirm.equalsIgnoreCase("Y")) {
            foundStudent.addGrade(grade);

            // save grade record
            Grade g = new Grade(nextGradeID, foundStudent.getStudentID(), subject.getSubjectName(), subject.getSubjectType(), grade, date);
            grades[gradeCount++] = g;

            System.out.printf("Grade recorded successfully Grade ID: %s%n", nextGradeID);
        } else {
            System.out.println("Grade recording canceled.");
        }
    }

    private static void viewGradeReport(Scanner sc) {
        System.out.println("VIEW GRADE REPORT");
        System.out.println("_________________________");
        System.out.print("Enter Student ID: ");
        String studentID = sc.nextLine();

        Student foundStudent = findStudentById(studentID);
        if (foundStudent == null) {
            System.out.println("Student not found.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        System.out.printf("Student: %s - %s%n", foundStudent.getStudentID(), foundStudent.getName());
        System.out.printf("Type: %s%n", (foundStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student");

        // collect grades for this student from the central grades array
        List<Grade> studentGrades = new ArrayList<>();
        for (int i = 0; i < gradeCount; i++) {
            Grade g = grades[i];
            if (g != null && g.getStudentID().equalsIgnoreCase(foundStudent.getStudentID())) {
                studentGrades.add(g);
            }
        }

        if (studentGrades.isEmpty()) {
            System.out.printf("Passing Grade: %d%%%n", foundStudent.getPassingGrade());
            System.out.println("No grades recorded for this student.");
        } else {
            double total = 0.0;
            int count = 0;
            double coreTotal = 0;
            int coreCount = 0;
            double electiveTotal = 0;
            int electiveCount = 0;

            System.out.println("\nGRADE HISTORY");
            System.out.println("_________________________________________________________________________");
            System.out.println("| GRD ID   | DATE        | SUBJECT         | TYPE            | GRADE    |");
            System.out.println("|________________________________________________________________________|");


            for (Grade gr : studentGrades) {
                System.out.printf("| %-8s | %-10s | %-15s | %-15s | %-8.1f |%n",
                        gr.getGradeID(),
                        sdf.format(gr.getDate()),
                        gr.getSubjectName(),
                        gr.getSubjectType(),
                        gr.getValue());
                total += gr.getValue();
                count++;

                if ("Core Subject".equals(gr.getSubjectType())) {
                    coreTotal += gr.getValue();
                    coreCount++;
                } else {
                    electiveTotal += gr.getValue();
                    electiveCount++;
                }
            }
            System.out.println("|___________________________________________________________________________________________________________|");


            double average = (count > 0) ? (total / count) : 0.0;
            System.out.printf("%nCurrent Average: %.1f%%%n", average);
            System.out.printf("Status: %s%n", (foundStudent.isPassing() ? "PASSING" : "FAILING"));

            System.out.println("\nTotal Grades: " + count);
            if (coreCount > 0) {
                System.out.printf("Core Subjects Average: %.1f%%%n", (coreTotal / coreCount));
            }
            if (electiveCount > 0) {
                System.out.printf("Elective Subjects Average: %.1f%%%n", (electiveTotal / electiveCount));
            }

            System.out.println("\nPerformance Summary:");
            if (foundStudent.isPassing()) {
                System.out.println("Passing all Core subjects");
                System.out.printf("Meeting passing grade requirement (%d%%)%n", foundStudent.getPassingGrade());
            }

            System.out.printf("%s - %s%n",
                    (foundStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student",
                    (foundStudent instanceof HonorsStudent ?
                            "higher standards (passing grade: 60%, eligible for honors recognition)" :
                            "standard grading (passing grade: 50%)"));
        }
    }
    private static void initializeSampleStudents() {
        // Core subjects
        Subject math = new CoreSubject("Mathematics", "MATH101");
        Subject english = new CoreSubject("English", "ENG101");
        Subject science = new CoreSubject("Science", "SCI101");
        // Elective subjects
        Subject art = new ElectiveSubject("Art", "ART101");
        Subject pe = new ElectiveSubject("Physical Education", "PE101");
    
        Date now = new Date();
        
        // Add 3 regular students
        Student s1 = new RegularStudent("Kofi Mensah", 20, "john@example.com", "1234567890");
        s1.enrollSubject(math); s1.enrollSubject(english); s1.enrollSubject(science);
        s1.addGrade(100); grades[gradeCount++] = new Grade("GRD001", s1.getStudentID(), "Mathematics", "Core Subject", 100, now);
        s1.addGrade(100); grades[gradeCount++] = new Grade("GRD002", s1.getStudentID(), "English", "Core Subject", 100, now);
        s1.addGrade(100); grades[gradeCount++] = new Grade("GRD003", s1.getStudentID(), "Science", "Core Subject", 100, now);
        
        Student s2 = new RegularStudent("Yaa Agyei", 21, "jane@example.com", "2345678901");
        s2.enrollSubject(math); s2.enrollSubject(english); s2.enrollSubject(art);
        s2.addGrade(60); grades[gradeCount++] = new Grade("GRD004", s2.getStudentID(), "Mathematics", "Core Subject", 60, now);
        s2.addGrade(55); grades[gradeCount++] = new Grade("GRD005", s2.getStudentID(), "English", "Core Subject", 55, now);
        s2.addGrade(70); grades[gradeCount++] = new Grade("GRD006", s2.getStudentID(), "Art", "Elective Subject", 70, now);
    
        Student s3 = new RegularStudent("John Cena", 22, "mike@example.com", "3456789012");
        s3.enrollSubject(science); s3.enrollSubject(art); s3.enrollSubject(pe);
        s3.addGrade(40); grades[gradeCount++] = new Grade("GRD007", s3.getStudentID(), "Science", "Core Subject", 40, now);
        s3.addGrade(50); grades[gradeCount++] = new Grade("GRD008", s3.getStudentID(), "Art", "Elective Subject", 50, now);
        s3.addGrade(60); grades[gradeCount++] = new Grade("GRD009", s3.getStudentID(), "Physical Education", "Elective Subject", 60, now);
    
        // Add 2 honors students
        Student s4 = new HonorsStudent("Afia Oduro", 20, "sarah@example.com", "4567890123");
        s4.enrollSubject(math); s4.enrollSubject(english); s4.enrollSubject(pe);
        s4.addGrade(85); grades[gradeCount++] = new Grade("GRD010", s4.getStudentID(), "Mathematics", "Core Subject", 85, now);
        s4.addGrade(90); grades[gradeCount++] = new Grade("GRD011", s4.getStudentID(), "English", "Core Subject", 90, now);
        s4.addGrade(88); grades[gradeCount++] = new Grade("GRD012", s4.getStudentID(), "Physical Education", "Elective Subject", 88, now);
    
        Student s5 = new HonorsStudent("David Goliath", 21, "david@example.com", "5678901234");
        s5.enrollSubject(science); s5.enrollSubject(art); s5.enrollSubject(pe);
        s5.addGrade(70); grades[gradeCount++] = new Grade("GRD013", s5.getStudentID(), "Science", "Core Subject", 70, now);
        s5.addGrade(65); grades[gradeCount++] = new Grade("GRD014", s5.getStudentID(), "Art", "Elective Subject", 65, now);
        s5.addGrade(75); grades[gradeCount++] = new Grade("GRD015", s5.getStudentID(), "Physical Education", "Elective Subject", 75, now);
    
        students[studentCount++] = s1;
        students[studentCount++] = s2;
        students[studentCount++] = s3;
        students[studentCount++] = s4;
        students[studentCount++] = s5;
    }
}