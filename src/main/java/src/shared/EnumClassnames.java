package src.shared;

public enum EnumClassnames {
    EXAMPLE_ENUM_CLASS("src.shared.EnumClass");

    private final String enumClassname;

    private EnumClassnames(String enumClassname) {
        this.enumClassname = enumClassname;
    }

    @Override
    public String toString() {
        return enumClassname;
    }
}
