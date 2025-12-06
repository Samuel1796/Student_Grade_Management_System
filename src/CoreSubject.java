// src/CoreSubject.java

// Represents a core subject with specific attributes and methods
//  Inherits from Subject class
public class CoreSubject extends Subject {
    public CoreSubject(String name, String code) {
        super(name, code);
    }

    public String getSubjectType() {
        return "Core Subject";
    }

    public void displaySubjectDetails() {
        System.out.println(getSubjectType());
    }
}
