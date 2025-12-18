package test;

import exceptions.AppExceptions;
import exceptions.InvalidGradeException;
import exceptions.DuplicateStudentException;
import models.Grade;
import models.RegularStudent;
import models.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.file.GradeService;
import services.student.StudentService;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GradeService (v2.0).
 * Focuses on recording grades, capacity limits, validation, duplication, and updates.
 */
class GradeServiceTest {

    private GradeService gradeService;
    private StudentService studentService;
    private Student student;

    @BeforeEach
    void setUp() throws DuplicateStudentException {
        gradeService = new GradeService(10);
        studentService = new StudentService();
        student = new RegularStudent("Samuel B", 21, "das@gmail.com", "0241234567");
        studentService.addStudent(student);
    }

    @Test
    void testRecordGradeStoresGradeAndIncrementsCount() {
        Grade grade = new Grade("GRD001", student.getStudentID(),
                "English", "Core Subject", 89, new Date());

        assertTrue(gradeService.recordGrade(grade, studentService));
        assertEquals(1, gradeService.getGradeCount());
        assertEquals("GRD001", gradeService.getGrades()[0].getGradeID());
    }

    @Test
    void testRecordGradeWithoutStudentServiceIsUnsupported() {
        Grade grade = new Grade("GRD001", student.getStudentID(),
                "English", "Core Subject", 89, new Date());
        assertThrows(UnsupportedOperationException.class,
                () -> gradeService.recordGrade(grade));
    }

    @Test
    void testGradeDatabaseFullThrowsAppExceptions() throws DuplicateStudentException {
        GradeService smallService = new GradeService(1);
        // reuse existing studentService and student
        Grade grade1 = new Grade("GRD001", student.getStudentID(),
                "English", "Core Subject", 89, new Date());
        Grade grade2 = new Grade("GRD002", student.getStudentID(),
                "Science", "Core Subject", 75, new Date());

        assertTrue(smallService.recordGrade(grade1, studentService));
        assertThrows(AppExceptions.class,
                () -> smallService.recordGrade(grade2, studentService));
    }

    @Test
    void testIsDuplicateGradeDetectsExistingGrade() {
        Grade grade = new Grade("GRD001", student.getStudentID(),
                "English", "Core Subject", 89, new Date());
        gradeService.recordGrade(grade, studentService);

        assertTrue(gradeService.isDuplicateGrade(student.getStudentID(), "English", "Core Subject"));
        assertFalse(gradeService.isDuplicateGrade(student.getStudentID(), "Math", "Core Subject"));
    }

    @Test
    void testUpdateGradeChangesStoredValue() {
        Grade grade = new Grade("GRD001", student.getStudentID(),
                "English", "Core Subject", 89, new Date());
        gradeService.recordGrade(grade, studentService);

        gradeService.updateGrade(student.getStudentID(), "English", "Core Subject", 1);
        assertEquals(1.0, gradeService.getGrades()[0].getValue());
    }

    @Test
    void testRecordInvalidGradeNegativeThrowsInvalidGradeException() {
        Grade invalidGrade = new Grade("GRD002", student.getStudentID(),
                "Math", "Core Subject", -5, new Date());
        assertThrows(InvalidGradeException.class,
                () -> gradeService.recordGrade(invalidGrade, studentService));
    }

    @Test
    void testRecordInvalidGradeAbove100ThrowsInvalidGradeException() {
        Grade invalidGrade = new Grade("GRD003", student.getStudentID(),
                "Math", "Core Subject", 105, new Date());
        assertThrows(InvalidGradeException.class,
                () -> gradeService.recordGrade(invalidGrade, studentService));
    }
}


