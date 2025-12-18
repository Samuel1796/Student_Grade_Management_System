//package test;
//
//import exceptions.DuplicateStudentException;
//import models.Grade;
//import models.RegularStudent;
//import models.Student;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import services.file.GradeImportExportService;
//import services.file.GradeService;
//import services.student.StudentService;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Lightweight integration tests covering interactions between
// * GradeService, GradeImportExportService, and StudentService.
// */
//class IntegrationTest {
//
//    private GradeService gradeService;
//    private GradeImportExportService importExportService;
//    private StudentService studentService;
//    private Student student;
//
//    @BeforeEach
//    void setUp() throws DuplicateStudentException {
//        gradeService = new GradeService(10);
//        importExportService = new GradeImportExportService(gradeService);
//        studentService = new StudentService();
//
//        student = new RegularStudent("Integration User", 22, "integration@example.com", "0241112223");
//        studentService.addStudent(student);
//    }
//
//    @Test
//    void testRecordGradeAndViewGradeReportDoesNotThrow() {
//        Grade grade = new Grade("GRD001", student.getStudentID(),
//                "Mathematics", "Core Subject", 89, new Date());
//        assertTrue(gradeService.recordGrade(grade, studentService));
//
//        assertDoesNotThrow(() -> gradeService.viewGradeReport(student));
//        assertEquals(1, gradeService.countGradesForStudent(student));
//    }
//
//    @Test
//    void testExportGradeReportCreatesReportFile() throws IOException {
//        Grade grade = new Grade("GRD002", student.getStudentID(),
//                "Science", "Core Subject", 85, new Date());
//        gradeService.recordGrade(grade, studentService);
//
//        String path = importExportService.exportGradeReport(student, 3, "integration_export");
//        File report = new File(path);
//
//        assertTrue(path.endsWith("integration_export.txt"));
//        assertTrue(report.exists());
//        assertTrue(report.length() > 0);
//    }
//
//    @Test
//    void testMultipleGradesCountedCorrectly() {
//        Grade grade1 = new Grade("GRD011", student.getStudentID(),
//                "Mathematics", "Core Subject", 75, new Date());
//        Grade grade2 = new Grade("GRD012", student.getStudentID(),
//                "English", "Core Subject", 85, new Date());
//
//        gradeService.recordGrade(grade1, studentService);
//        gradeService.recordGrade(grade2, studentService);
//
//        assertEquals(2, gradeService.countGradesForStudent(student));
//    }
//}
//
//
