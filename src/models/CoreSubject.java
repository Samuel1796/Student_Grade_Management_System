package models;

public class CoreSubject implements Subject {
    private String subjectName;
    private String subjectCode;

    public CoreSubject(String name, String code) {
        this.subjectName = name;
        this.subjectCode = code;
    }

    @Override
    public String getSubjectType() {
        return "Core Subject";
    }

    @Override
    public void displaySubjectDetails() {
        System.out.println(getSubjectType());
    }

    @Override
    public String getSubjectName() {
        return subjectName;
    }

    @Override
    public String getSubjectCode() {
        return subjectCode;
    }
}