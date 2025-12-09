package services;

import models.Grade;
import models.HonorsStudent;
import models.Student;
import models.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import exceptions.StudentNotFoundException;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntegrationTest {

    private GradeService gradeService;
    private StudentService mockStudentService;
    private HonorsStudent mockStudent;
    private Subject mockSubject;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService(10);
        mockStudentService = mock(StudentService.class);
        mockStudent = mock(HonorsStudent.class);
        mockSubject = mock(Subject.class);

        when(mockStudent.getStudentID()).thenReturn("STU001");
        when(mockStudent.getName()).thenReturn("Test Student");
        when(mockStudent.getPassingGrade()).thenReturn(60);
        when(mockStudent.isPassing(any(GradeService.class))).thenReturn(true);
        when(mockStudent.calculateAverage(any(GradeService.class))).thenReturn(80.0);
    }

    @Test
    void testRecordGradeAndViewGradeReport() {
        Grade grade = new Grade("GRD001", "STU001", "Mathematics", "Core Subject", 2, new Date());
        gradeService.recordGrade(grade);

//        TODOS:
//        Even if i record 0 as a grade for an honors student, it still returns FAILING, (bug present)
//        Same for the meeting grade requirement for honors students


        // Should not throw any exceptions
        assertDoesNotThrow(() -> gradeService.viewGradeReport(mockStudent));
        assertEquals(1, gradeService.getGradeCount());
    }

    @Test
    void testExportGradeReportWithMockedStudent() throws IOException {
        Grade grade = new Grade("GRD002", "STU001", "Science", "Core Subject", 85, new Date());
        gradeService.recordGrade(grade);

        String filePath = gradeService.exportGradeReport(mockStudent, 3, "test_export");
        assertTrue(filePath.contains("test_export.txt"));
    }

    @Test
    void testBulkImportGradesWithMockedStudentService() {
        // Setup mock for findStudentById and findSubjectByNameAndType
        when(mockStudentService.findStudentById("S001")).thenReturn(mockStudent);
        when(mockStudentService.findSubjectByNameAndType("Mathematics", "Core Subject")).thenReturn(mockSubject);

        // Create a temporary CSV file for import
        String csvFileName = "test_import";
        String csvFilePath = "./imports/" + csvFileName + ".csv";
        try {
            java.io.File importsDir = new java.io.File("./imports");
            if (!importsDir.exists()) importsDir.mkdir();
            java.io.FileWriter fw = new java.io.FileWriter(csvFilePath);
            fw.write("StudentID,SubjectName,SubjectType,Grade\n");
            fw.write("S001,Mathematics,Core Subject,95\n");
            fw.close();

            gradeService.bulkImportGrades(csvFileName, mockStudentService);

            assertEquals(1, gradeService.getGradeCount());
        } catch (IOException e) {
            fail("IOException during bulk import test: " + e.getMessage());
        } finally {
            new java.io.File(csvFilePath).delete();
        }
    }





}