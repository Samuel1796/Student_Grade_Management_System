//package test;
//
//import org.junit.jupiter.api.Test;
//import utilities.ValidationUtils;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Unit tests for ValidationUtils regex patterns and error-message helpers.
// */
//class ValidationUtilsTest {
//
//    @Test
//    void testValidStudentIdPatterns() {
//        assertTrue(ValidationUtils.isValidStudentId("STU001"));
//        assertTrue(ValidationUtils.isValidStudentId("STU999"));
//    }
//
//    @Test
//    void testInvalidStudentIdPatterns() {
//        assertFalse(ValidationUtils.isValidStudentId("stu001"));   // lowercase prefix
//        assertFalse(ValidationUtils.isValidStudentId("STU01"));    // too short
//        assertFalse(ValidationUtils.isValidStudentId("STU1000"));  // too long
//        assertFalse(ValidationUtils.isValidStudentId(null));
//    }
//
//    @Test
//    void testValidEmailPatterns() {
//        assertTrue(ValidationUtils.isValidEmail("john.doe@example.com"));
//        assertTrue(ValidationUtils.isValidEmail("user123@domain.co.uk"));
//    }
//
//    @Test
//    void testInvalidEmailPatterns() {
//        assertFalse(ValidationUtils.isValidEmail("no-at-symbol"));
//        assertFalse(ValidationUtils.isValidEmail("user@domain"));
//        assertFalse(ValidationUtils.isValidEmail("user@domain.c")); // TLD too short
//        assertFalse(ValidationUtils.isValidEmail(null));
//    }
//
//    @Test
//    void testValidGhanaPhoneNumbers() {
//        assertTrue(ValidationUtils.isValidPhone("0241234567"));
//        assertTrue(ValidationUtils.isValidPhone("0509876543"));
//        assertTrue(ValidationUtils.isValidPhone("+233241234567"));
//        assertTrue(ValidationUtils.isValidPhone("+233509876543"));
//    }
//
//    @Test
//    void testInvalidGhanaPhoneNumbers() {
//        assertFalse(ValidationUtils.isValidPhone("241234567"));        // missing leading 0
//        assertFalse(ValidationUtils.isValidPhone("024123456"));        // too short
//        assertFalse(ValidationUtils.isValidPhone("+23324123456"));     // too short after +233
//        assertFalse(ValidationUtils.isValidPhone("233241234567"));     // missing +
//        assertFalse(ValidationUtils.isValidPhone(null));
//    }
//
//    @Test
//    void testValidNamePatterns() {
//        assertTrue(ValidationUtils.isValidName("John Smith"));
//        assertTrue(ValidationUtils.isValidName("Mary-Jane Watson"));
//        assertTrue(ValidationUtils.isValidName("O'Brien"));
//    }
//
//    @Test
//    void testInvalidNamePatterns() {
//        assertFalse(ValidationUtils.isValidName(""));                // empty
//        assertFalse(ValidationUtils.isValidName(" "));               // blank
//        assertFalse(ValidationUtils.isValidName("John123"));         // digits
//        assertFalse(ValidationUtils.isValidName("John@Smith"));      // symbol
//    }
//
//    @Test
//    void testValidDatePatterns() {
//        assertTrue(ValidationUtils.isValidDate("2025-12-17"));
//        assertTrue(ValidationUtils.isValidDate("2024-01-01"));
//    }
//
//    @Test
//    void testInvalidDatePatterns() {
//        assertFalse(ValidationUtils.isValidDate("17-12-2025"));
//        assertFalse(ValidationUtils.isValidDate("2025/12/17"));
//        assertFalse(ValidationUtils.isValidDate("2025-13-01"));
//    }
//
//    @Test
//    void testValidCourseCodePatterns() {
//        assertTrue(ValidationUtils.isValidCourseCode("MAT101"));
//        assertTrue(ValidationUtils.isValidCourseCode("ENG203"));
//    }
//
//    @Test
//    void testInvalidCourseCodePatterns() {
//        assertFalse(ValidationUtils.isValidCourseCode("mat101"));  // lowercase
//        assertFalse(ValidationUtils.isValidCourseCode("MA101"));   // too short
//        assertFalse(ValidationUtils.isValidCourseCode("MATH101")); // too long
//    }
//
//    @Test
//    void testValidGradeStringPatterns() {
//        assertTrue(ValidationUtils.isValidGrade("0"));
//        assertTrue(ValidationUtils.isValidGrade("5"));
//        assertTrue(ValidationUtils.isValidGrade("99"));
//        assertTrue(ValidationUtils.isValidGrade("100"));
//    }
//
//    @Test
//    void testInvalidGradeStringPatterns() {
//        assertFalse(ValidationUtils.isValidGrade("-1"));
//        assertFalse(ValidationUtils.isValidGrade("101"));
//        assertFalse(ValidationUtils.isValidGrade("05"));   // leading zero not allowed except "0"
//        assertFalse(ValidationUtils.isValidGrade("abc"));
//    }
//
//    @Test
//    void testValidGradeDouble() {
//        assertTrue(ValidationUtils.isValidGrade(0));
//        assertTrue(ValidationUtils.isValidGrade(50.5));
//        assertTrue(ValidationUtils.isValidGrade(100));
//        assertFalse(ValidationUtils.isValidGrade(-0.1));
//        assertFalse(ValidationUtils.isValidGrade(100.1));
//    }
//
//    @Test
//    void testValidateEmailReturnsErrorForInvalid() {
//        String error = ValidationUtils.validateEmail("bad-email");
//        assertNotNull(error);
//        assertTrue(error.contains("Invalid Email"));
//        assertTrue(error.contains("standard email format"));
//
//        assertNull(ValidationUtils.validateEmail("valid.user@example.com"));
//    }
//
//    @Test
//    void testValidatePhoneReturnsErrorForInvalid() {
//        String error = ValidationUtils.validatePhone("123-456-7890"); // non-Ghana format
//        assertNotNull(error);
//        assertTrue(error.contains("Invalid Phone"));
//        assertTrue(error.contains("Ghana phone format"));
//
//        assertNull(ValidationUtils.validatePhone("0241234567"));
//    }
//}
//
//
