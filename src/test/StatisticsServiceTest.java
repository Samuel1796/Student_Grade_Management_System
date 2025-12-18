package test;

import models.Grade;
import models.RegularStudent;
import models.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.analytics.StatisticsService;
import services.file.GradeService;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatisticsService (v2.0).
 * Verifies statistical calculations over grades and students.
 */
class StatisticsServiceTest {

    private GradeService gradeService;
    private StatisticsService statisticsService;
    private Student student;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService(10);
        student = new RegularStudent("Liu Kang", 21, "kang@ex.com", "0241234567");

        Grade grade1 = new Grade("GRD001", student.getStudentID(), "Mathematics", "Core Subject", 100, new Date());
        Grade grade2 = new Grade("GRD002", student.getStudentID(), "English", "Core Subject", 70, new Date());
        Grade grade3 = new Grade("GRD003", student.getStudentID(), "Science", "Core Subject", 40, new Date());

        // record via GradeService API requires a StudentService in production, but for pure statistics
        // we can populate the internal array directly and set gradeCount.
        Grade[] grades = gradeService.getGrades();
        grades[0] = grade1;
        grades[1] = grade2;
        grades[2] = grade3;
        gradeService.setGradeCount(3);

        statisticsService = new StatisticsService(
                gradeService.getGrades(),
                gradeService.getGradeCount(),
                Arrays.asList(student),
                1,
                gradeService
        );
    }

    @Test
    void testCalculateMean() {
        assertEquals(70.0, statisticsService.calculateMean(), 1e-9);
    }

    @Test
    void testCalculateMedian() {
        assertEquals(70.0, statisticsService.calculateMedian(), 1e-9);
    }

    @Test
    void testCalculateModeNoModeReturnsZero() {
        assertEquals(0.0, statisticsService.calculateMode(), 1e-9);
    }

    @Test
    void testCalculateStdDev() {
        assertEquals(24.49489742783178, statisticsService.calculateStdDev(), 1e-9);
    }

    @Test
    void testCalculateRange() {
        assertEquals(60.0, statisticsService.calculateRange(), 1e-9);
    }

    @Test
    void testGetHighestAndLowestGrade() {
        assertEquals(100.0, statisticsService.getHighestGrade(), 1e-9);
        assertEquals(40.0, statisticsService.getLowestGrade(), 1e-9);
    }

    @Test
    void testGetSubjectAverages() {
        Map<String, Double> subjectAverages = statisticsService.getSubjectAverages();
        assertEquals(100.0, subjectAverages.get("Mathematics"), 1e-9);
        assertEquals(70.0, subjectAverages.get("English"), 1e-9);
        assertEquals(40.0, subjectAverages.get("Science"), 1e-9);
        assertNull(subjectAverages.get("NonExistentSubject"));
    }

    @Test
    void testGetGradeDistribution() {
        Map<String, Integer> dist = statisticsService.getGradeDistribution();
        assertEquals(1, dist.get("0-59"));   // 40
        assertEquals(0, dist.get("60-69"));  // none
        assertEquals(1, dist.get("70-79"));  // 70
        assertEquals(0, dist.get("80-89"));  // none
        assertEquals(1, dist.get("90-100")); // 100
    }

    @Test
    void testGetStudentTypeAverages() {
        Map<String, Double> typeAvgs = statisticsService.getStudentTypeAverages();
        assertEquals(70.0, typeAvgs.get("Regular Students"), 1e-9);
        assertEquals(0.0, typeAvgs.get("Honors Students"), 1e-9);
    }
}


