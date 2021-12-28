package xyz.nsgw.objects;

public class PermissibleUser extends User {

    private UserPermissionRecord permissions;

    public PermissibleUser(String name) {
        super(name);
    }
}
