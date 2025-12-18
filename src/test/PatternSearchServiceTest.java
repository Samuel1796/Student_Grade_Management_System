//package test;
//
//import models.Student;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import services.search.PatternSearchService;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Unit tests for PatternSearchService (US-7).
// * Focuses on regex and wildcard behaviour plus basic statistics.
// */
//class PatternSearchServiceTest {
//
//    private Student s1;
//    private Student s2;
//    private Collection<Student> students;
//
//    /**
//     * Minimal Student implementation for search-focused tests.
//     * Only the fields used by PatternSearchService are meaningfully implemented.
//     */
//    private static class TestStudent implements Student {
//        private final String id;
//        private final String name;
//        private final String email;
//        private final String phone;
//
//        TestStudent(String id, String name, String email, String phone) {
//            this.id = id;
//            this.name = name;
//            this.email = email;
//            this.phone = phone;
//        }
//
//        @Override public String getStudentID() { return id; }
//        @Override public String getName() { return name; }
//        @Override public String getEmail() { return email; }
//        @Override public String getPhone() { return phone; }
//
//        // Unused in PatternSearchService – stub implementations
//        @Override public void addGrade(double grade) { }
//        @Override public double calculateAverage(services.file.GradeService gradeService) { return 0; }
//        @Override public boolean isPassing(services.file.GradeService gradeService) { return false; }
//        @Override public boolean isHonorsEligible(services.file.GradeService gradeService) { return false; }
//        @Override public int getPassingGrade() { return 0; }
//        @Override public int getGradeCount() { return 0; }
//        @Override public void enrollSubject(models.Subject subject) { }
//        @Override public String getEnrolledSubjectsString() { return ""; }
//        @Override public int getAge() { return 0; }
//        @Override public String getStatus() { return ""; }
//        @Override public java.util.List<models.Subject> getEnrolledSubjects() { return Collections.emptyList(); }
//        @Override public void setName(String name) { }
//        @Override public void setAge(int age) { }
//        @Override public void setEmail(String email) { }
//        @Override public void setPhone(String phone) { }
//        @Override public void setStatus(String status) { }
//    }
//
//    @BeforeEach
//    void setUp() {
//        s1 = new TestStudent("STU001", "John Doe", "john.doe@example.com", "0241234567");
//        s2 = new TestStudent("STU002", "Jane Smith", "jane@school.edu", "+233241234567");
//        students = Arrays.asList(s1, s2);
//    }
//
//    @Test
//    void testSearchByEmailDomainCaseInsensitive() {
//        PatternSearchService service = new PatternSearchService(students);
//
//        Map<String, Object> result = service.searchByEmailDomain("@EXAMPLE.COM", false);
//        @SuppressWarnings("unchecked")
//        List<PatternSearchService.SearchResult> results =
//                (List<PatternSearchService.SearchResult>) result.get("results");
//        PatternSearchService.SearchStatistics stats =
//                (PatternSearchService.SearchStatistics) result.get("statistics");
//
//        assertEquals(2, stats.getTotalScanned());
//        assertEquals(1, stats.getMatchesFound());
//        assertEquals(s1, results.get(0).getStudent());
//        assertTrue(results.get(0).getHighlightedMatch().contains(">>>"));
//    }
//
//    @Test
//    void testSearchByStudentIdPatternWithWildcards() {
//        PatternSearchService service = new PatternSearchService(students);
//
//        // ? should match exactly one character
//        Map<String, Object> result = service.searchByStudentIdPattern("STU00?", true);
//        @SuppressWarnings("unchecked")
//        List<PatternSearchService.SearchResult> results =
//                (List<PatternSearchService.SearchResult>) result.get("results");
//
//        assertEquals(2, results.size()); // STU001 and STU002 both match
//    }
//
//    @Test
//    void testSearchByNamePattern() {
//        PatternSearchService service = new PatternSearchService(students);
//
//        Map<String, Object> result = service.searchByNamePattern("Jane", false);
//        @SuppressWarnings("unchecked")
//        List<PatternSearchService.SearchResult> results =
//                (List<PatternSearchService.SearchResult>) result.get("results");
//
//        assertEquals(1, results.size());
//        assertEquals("name", results.get(0).getMatchedField());
//        assertEquals(s2, results.get(0).getStudent());
//    }
//
//    @Test
//    void testSearchByCustomPatternInvalidRegexReturnsError() {
//        PatternSearchService service = new PatternSearchService(students);
//
//        Map<String, Object> result = service.searchByCustomPattern("*(unclosed", false);
//        assertTrue(result.containsKey("error"));
//        assertEquals("*(unclosed", result.get("pattern"));
//    }
//
//    @Test
//    void testSearchByCustomPatternMatchesMultipleFields() {
//        PatternSearchService service = new PatternSearchService(students);
//
//        // Matches any address at example.com or school.edu
//        Map<String, Object> result = service.searchByCustomPattern(".*@(example\\.com|school\\.edu)", false);
//        @SuppressWarnings("unchecked")
//        List<PatternSearchService.SearchResult> results =
//                (List<PatternSearchService.SearchResult>) result.get("results");
//
//        assertEquals(2, results.size());
//    }
//}
//
//
