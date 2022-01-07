package xyz.nsgw.tools.nscript;
// An object used to match an identifier with its value.
// Used in nscript when setting variables and calling methods.
public class Reference {
    // Initialize the reference's local identifier
    private final String identifier;
    // and value
    private String value;
    // Constructor - take the identifier if the value hasn't already been provided.
    public Reference(String identifier) {
        // Strip / trim the identifier of any trailing whitespace.
        this.identifier = identifier.stripTrailing();
    }
    // Constructor - take and set the identifier and the value.
    public Reference(String identifier, String value) {
        this.identifier = identifier.stripLeading().stripTrailing();
        this.value = value.stripLeading().stripTrailing();
    }
    // Get the value.
    public String getValue() {
        return value;
    }
    // Get the identifier.
    public String getIdentifier() {
        return identifier;
    }
    // Strip / trim and set a new value.
    public void setValue(String value) {
        this.value = value.stripLeading().stripTrailing();
    }
}
