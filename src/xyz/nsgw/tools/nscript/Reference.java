package xyz.nsgw.tools.nscript;

public class Reference {

    private final String identifier;

    private String value;

    public Reference(String identifier) {
        this.identifier = identifier.stripTrailing();
    }

    public Reference(String identifier, String value) {
        this.identifier = identifier.stripLeading().stripTrailing();
        this.value = value.stripLeading().stripTrailing();
    }

    public String getValue() {
        return value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setValue(String value) {
        this.value = value.stripLeading().stripTrailing();
    }
}
