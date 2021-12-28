package xyz.nsgw.objects.user;

public class PermissibleUser extends User {

    private UserPermissionRecord permissions;

    public PermissibleUser(String name) {
        super(name);
    }
}
