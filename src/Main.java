import java.sql.SQLOutput;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static Student[] students  = new Student[50];
    private static int studentCount = 0;


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
                    break;
                case 5:
                    System.out.println("Thank you for using the system ");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, Try again ");
            }
            System.out.println();


        }
        sc.close();

    }

    public static void displayMenu() {
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Exit");
        System.out.println();
    }

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

        System.out.print("Enter student mail: ");
        String email = sc.nextLine();

        System.out.print("Enter student phone: ");
        String phone = sc.nextLine();
        System.out.println();

        System.out.println("Student type: ");
        System.out.println("1. Regular Student (Passing grade: 50%)");
        System.out.println("2. Honors Student (Passing grade: 60%, honors recognition)");
        System.out.print("Select type (1-2): ");
        int type = sc.nextInt();
        sc.nextLine();
        System.out.println();

        Student newStudent;  // Declare the variable
        
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

//        Get the type of student(at runtime for displ)
        String typeName = (newStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student";


        System.out.println("Student added successfully!");
        System.out.println("_______________________________");

        System.out.printf("Student ID: %s",newStudent.getStudentID());
        System.out.println();
        System.out.printf("Name: %s", newStudent.getName());
        System.out.println();
        System.out.printf("Type: %s", typeName);
        System.out.println();
        System.out.printf("Age: %d", newStudent.getAge());
        System.out.println();
        System.out.printf("Email: %s", newStudent.getEmail());
        System.out.println();
        System.out.printf("Passing Grade: %d", newStudent.getPassingGrade());
        System.out.println();
        System.out.println(newStudent.honorsEligible?"Honors Eligible: Yes": "");
        System.out.printf("Status: %s", newStudent.getStatus());
        System.out.println();
        System.out.println("_______________________________");








    }


    public static void viewStudents(){
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        System.out.println();
        System.out.println("STUDENT LISTING");
        System.out.println("________________________________________________________________________________________________________________________");
        System.out.println("| STUDENT ID  | NAME                  | TYPE            | AVG GRADE | STATUS       | ENROLLED SUBJECTS    | PASSING GRADE | Honors Eligible     |");
        System.out.println("|_______________________________________________________________________________________________________________________|");
        for (int i = 0; i < studentCount; i++) {
            Student student = students[i];
            System.out.printf("| %s      | %-15s       | %-8s        | %-2d        | %-2s        | %-3s   | %2d      |    %2s    |\n",
                    student.getStudentID(),
                    student.getName(),
                    (student instanceof HonorsStudent) ? "Honors" : "Regular",
                    student.getAge(),// Average Grade placeholder
                    student.getStatus(),
                    student.getEmail(),
                    student.getPassingGrade(),
                    student.isHonorsEligible() ? "Yes" : "");
        }
        System.out.println("|=========================================================================================================================|");
    }

    public static void recordGrade(Scanner sc) {
        System.out.println();
        System.out.println("RECORD GRADE");
        System.out.println("_________________________");

//        GET STUDENT ID
        System.out.print("Enter Student ID: ");
        String studentID = sc.nextLine();

//        FIND STUDENT BY ID
        Student foundStudent = null;
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentID().equals(studentID)) {
                foundStudent = students[i];
                break;
            }
        }

        if (foundStudent == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.println();
        System.out.println("Student Details: ");
        System.out.printf("Name: %s", foundStudent.getName());
        System.out.println();
        System.out.printf("Type: %s", (foundStudent instanceof HonorsStudent) ? "Honors Student" : "Regular Student");
        System.out.println();
        System.out.println("Current Average: ");



    };

    private static void initializeSampleStudents() {
        // Add 3 regular students
        students[studentCount++] = new RegularStudent("Kofi Mensah", 20, "john@example.com", "1234567890");
        students[studentCount++] = new RegularStudent("Yaa Agyei", 21, "jane@example.com", "2345678901");
        students[studentCount++] = new RegularStudent("John Cena", 22, "mike@example.com", "3456789012");

        // Add 2 honors students
        students[studentCount++] = new HonorsStudent("Afia Oduro", 20, "sarah@example.com", "4567890123");
        students[studentCount++] = new HonorsStudent("David Goliath", 21, "david@example.com", "5678901234");

        // Add sample grades
        students[0].addGrade(75); // John
        students[0].addGrade(80);
        students[1].addGrade(60); // Jane
        students[1].addGrade(55);
        students[2].addGrade(40); // Mike
        students[2].addGrade(50);
        students[3].addGrade(85); // Sarah
        students[3].addGrade(90);
        students[4].addGrade(70); // David
        students[4].addGrade(65);
    }

}