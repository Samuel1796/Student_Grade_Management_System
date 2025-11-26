public abstract class CoreSubject extends Subject {
    public CoreSubject(String name, String code) {
        super(name, code);
    }

    public String getSubjectType() {
        return "Core Subject";
    }
}