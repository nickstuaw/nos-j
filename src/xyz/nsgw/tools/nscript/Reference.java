package xyz.nsgw.tools.nscript;

public class Reference {

    private String identifier;

    private String value;

    public Reference(String identifier) {
        this.identifier = identifier.stripTrailing();
    }

    public Reference(String identifier, String argument) {
        this.identifier = identifier.stripLeading().stripTrailing();
        this.value = argument.stripLeading().stripTrailing();
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

    public void setIdentifier(String identifier) {
        this.identifier = identifier.stripLeading().stripTrailing();
    }
}
