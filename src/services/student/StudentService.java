package services.student;

import models.*;
import exceptions.DuplicateStudentException;
import exceptions.StudentNotFoundException;

import java.io.IOException;
import java.util.*;

import utilities.FileIOUtils;
import utilities.Logger;
import java.nio.file.Paths;

/**
 * Service class for managing student data.
 */

public class StudentService {
    private final HashMap<String, Student> studentMap = new HashMap<>();

    /**
     * Adds a new student to the system.
     * Throws DuplicateStudentException if a student with the same ID already exists.
     * @param student The student to add
     */
    public void addStudent(Student student) throws DuplicateStudentException {
        long startTime = System.currentTimeMillis();
        String normalizedId = student.getStudentID().toUpperCase();
        if (studentMap.containsKey(normalizedId)) {
            long duration = System.currentTimeMillis() - startTime;
            Logger.logAudit("ADD_STUDENT", "Add student: " + student.getStudentID(), duration, false, 
                "Duplicate student ID: " + normalizedId);
            throw new DuplicateStudentException(student.getName(), student.getEmail());
        }
        studentMap.put(normalizedId, student);
        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("studentCount", studentMap.size());
        Logger.logPerformance("ADD_STUDENT", duration, metrics);
        Logger.logAudit("ADD_STUDENT", "Add student: " + student.getStudentID(), duration, true, 
            "Student added successfully");
    }

    public Student findStudentById(String studentID) throws StudentNotFoundException {
        long startTime = System.currentTimeMillis();
        String normalizedId = studentID.toUpperCase();
        Student student = studentMap.get(normalizedId);
        long duration = System.currentTimeMillis() - startTime;
        if (student == null) {
            Logger.logAudit("FIND_STUDENT", "Find student: " + studentID, duration, false, 
                "Student not found: " + studentID);
            throw new StudentNotFoundException(studentID);
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("studentCount", studentMap.size());
        Logger.logPerformance("FIND_STUDENT", duration, metrics);
        Logger.logAudit("FIND_STUDENT", "Find student: " + studentID, duration, true, 
            "Student found: " + studentID);
        return student;
    }


    /**
     * Finds a subject by its name and type across all students in the system.
     * 
     * This method implements a linear search algorithm that:
     * 1. Iterates through all students
     * 2. Checks each student's enrolled subjects
     * 3. Matches subject name (case-insensitive) and type (by class instance)
     * 4. Returns first matching subject found
     * 
     * Search Strategy:
     * - Name matching: case-insensitive comparison for flexibility
     * - Type matching: uses instanceof to check class hierarchy
     * - Supports multiple type string formats for backward compatibility
     *
     * 
     * Design Considerations:
     * - Returns first match (assumes subject instances are equivalent)
     * - Null-safe: handles null subjects and null lists gracefully
     * - Type checking uses instanceof for reliable class hierarchy detection
     * 
     * @param name The subject name to search for (case-insensitive)
     * @param type The type ("Core", "Elective", "Core Subject", "Elective Subject")
     * @return The matching Subject instance, or null if not found
     */

    public Subject findSubjectByNameAndType(String name, String type) {
        // Linear search: iterate through all students
        for (Student s : getStudents()) {
            List<Subject> enrolledSubjects = s.getEnrolledSubjects();
            
            // Null-safe check: handle students with no enrolled subjects
            if (enrolledSubjects != null) {
                for (Subject subj : enrolledSubjects) {
                    if (subj != null && subj.getSubjectName().equalsIgnoreCase(name)) {
                        if ((type.equalsIgnoreCase("Core") && subj instanceof CoreSubject) ||
                                (type.equalsIgnoreCase("Elective") && subj instanceof ElectiveSubject) ||
                                (type.equalsIgnoreCase("Core Subject") && subj instanceof CoreSubject) ||
                                (type.equalsIgnoreCase("Elective Subject") && subj instanceof ElectiveSubject)) {
                            // Match found: return first matching subject
                            return subj;
                        }
                    }
                }
            }
        }
        // No match found: return null
        return null;
    }



    /**
     * Returns a collection of all students.
     */
    public Collection<Student> getStudents() {
        return studentMap.values();
    }

    /**
     * Returns the total number of students.
     */
    public int getStudentCount() {
        return studentMap.size();
    }

    /**
     * Checks if a student with the same name and email already exists.
     * @return true if duplicate exists, false otherwise
     */
    public boolean isDuplicateStudent(String name, String email) {
        for (Student s : studentMap.values()) {
            if (s.getName().equalsIgnoreCase(name) && s.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the format of an email address.
     * @return true if valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Searches for students by partial or full name match using case-insensitive substring matching.
     * 
     * - Converts student collection to stream
     * - Filters students whose name contains the search term (case-insensitive)
     * - Collects results into an array
     * 
     * Search Characteristics:
     * - Case-insensitive: "john" matches "John", "JOHN", "johnny"
     * - Substring matching: "john" matches "John Doe", "Johnny", "Little John"
     * - Partial matching: finds students even if search term is part of full name

     * Limitations:
     * - No ranking: results are in arbitrary order
     * - No exact match prioritization: "John" and "Johnny" treated equally
     *
     * @param namePart Search term (partial or full name, case-insensitive)
     * @return Array of Student objects whose names contain the search term
     */
    public Student[] searchStudentsByName(String namePart) {
        return studentMap.values().stream()
                // Filter: keep only students whose name contains search term
                // Case-insensitive: convert both to lowercase for comparison
                // contains() performs substring matching (not just prefix/suffix)
                .filter(s -> s.getName().toLowerCase().contains(namePart.toLowerCase()))
                // Collect results into array
                // toArray() creates new array with exact size needed
                .toArray(Student[]::new);
    }

    /**
     * Searches for students whose average grade falls within a specified range (inclusive).
     * 
     * This method implements range-based filtering:
     * - Calculates each student's average grade using GradeService
     * - Filters students whose average is within [min, max] range
     * - Returns all matching students
     * 
     * Range Matching:
     * - Inclusive bounds: students with exactly min or max are included
     * - Range validation: assumes min <= max (caller's responsibility)
     * - Handles edge cases: min = max finds students with exact average
     * Optimization Opportunities:
     * - Cache average grades to avoid recalculation
     * - Pre-compute averages during grade updates
     * - Use indexed data structure for range queries
     * 
     * Use Cases:
     * - Find high performers: min=90, max=100
     * - Find students needing support: min=0, max=60
     * - Find average students: min=70, max=80
     * 
     * @param min Minimum average grade (inclusive, 0-100)
     * @param max Maximum average grade (inclusive, 0-100)
     * @param gradeService Service for calculating student averages
     * @return Array of Student objects whose average grade is within the specified range
     */
    public Student[] searchStudentsByGradeRange(double min, double max, services.file.GradeService gradeService) {
        return studentMap.values().stream()
                .filter(s -> {
                    // Calculate average for this student
                    // This is called for every student - consider caching for performance
                    double avg = s.calculateAverage(gradeService);
                    
                    // Range check: inclusive bounds
                    // Returns true if average is within [min, max]
                    return avg >= min && avg <= max;
                })
                .toArray(Student[]::new);
    }


    /**
     * Searches for students by type (Honors or Regular) using instanceof check.
     * 
     * This method filters students based on their class hierarchy:
     * - Honors students: instances of HonorsStudent class
     * - Regular students: instances of RegularStudent class
     * 
     * Type Detection:
     * - Uses instanceof operator for reliable class hierarchy checking
     * - More reliable than string comparison or flag checking
     * - Works correctly with inheritance and polymorphism
     * 

     * Use Cases:
     * - Filter honors students: honors=true
     * - Filter regular students: honors=false
     * - Generate type-specific reports
     * - Apply type-specific policies
     * 
     * @param honors If true, returns Honors students; if false, returns Regular students
     * @return Array of Student objects matching the specified type
     */
    public Student[] searchStudentsByType(boolean honors) {
        // Stream API with type filtering; single pass O(n) over all students.
        return studentMap.values().stream()
                // Type check: compare desired type with actual instance type
                // honors == (s instanceof HonorsStudent) ensures boolean match
                // If honors=true, keeps only HonorsStudent instances
                // If honors=false, keeps only RegularStudent instances
                .filter(s -> honors == (s instanceof HonorsStudent))
                .toArray(Student[]::new);
    }

    /**
     * Displays all students with their basic information and average grade.
     **/
    public void viewAllStudents(services.file.GradeService gradeService) {
        System.out.println("________________________________________________________________________________________________________________________________________________");
        System.out.println("| STUDENT ID  | NAME                  | TYPE            | AVG GRADE | STATUS       | ENROLLED SUBJECTS    | PASSING GRADE | Honors Eligible     |");
        System.out.println("|_______________________________________________________________________________________________________________________________________________|");
    
        // Copy to ArrayList and sort by Student ID (overall O(n log n) for ordering plus O(n) copy).
        List<Student> sortedStudents = new ArrayList<>(studentMap.values());
        sortedStudents.sort(Comparator.comparing(Student::getStudentID, String.CASE_INSENSITIVE_ORDER));

        // If there are no students, show a clear empty-state message instead of a blank table
        if (sortedStudents.isEmpty()) {
            System.out.println("| No students found. Please add students first.                                                                                               |");
            System.out.println("|=============================================================================================================================================|");
            return;
        }
    
        for (Student student : sortedStudents) {
            String typeStr = (student instanceof HonorsStudent) ? "Honors" : "Regular";
            double avg = student.calculateAverage(gradeService);
            System.out.printf("| %-10s | %-20s | %-15s | %-9.1f | %-12s | %-20s | %-13d | %-18s |%n",
                    student.getStudentID(),
                    student.getName(),
                    typeStr,
                    avg,
                    student.getStatus(),
                    (student.getEnrolledSubjects() != null ? student.getEnrolledSubjects().size() : 0),
                    student.getPassingGrade(),
                    (student instanceof HonorsStudent ? ((HonorsStudent)student).isHonorsEligible(gradeService) : student.isHonorsEligible(gradeService)) ? "Yes" : "No");
        }
    
        System.out.println("|=============================================================================================================================================|");
    }










}