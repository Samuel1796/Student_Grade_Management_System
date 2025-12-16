package utilities;

public class StudentIdGenerator {
    private static int studentCounter = 0;

    public static synchronized String nextId() {
        studentCounter++;
        return String.format("STU%03d", studentCounter);
    }
}