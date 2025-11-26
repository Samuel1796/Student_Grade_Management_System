public abstract class Subject {
    protected String subjectName;
    protected String subjectCode;

    public Subject(String name, String code) {
        this.subjectName = name;
        this.subjectCode = code;
    }

    public abstract void displaySubjectDetails();
    public abstract String getSubjectType();

    // Getters
    public String getSubjectName() {
        return subjectName;
    }
    public String getSubjectCode() {
        return subjectCode;
    }
}
