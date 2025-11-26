public abstract class ElectiveSubject extends Subject {
    public ElectiveSubject(String name, String code) {
        super(name, code);
    }

    public String getSubjectType() {
        return "Elective Subject";
    }
}