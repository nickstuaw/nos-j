package xyz.nsgw.objects;

import java.util.ArrayList;
import java.util.List;

public class UserPermissionRecord {

    private List<UserPermissionEntry> entries;

    public UserPermissionRecord() {
        entries = new ArrayList<>();
    }

    public List<UserPermissionEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<UserPermissionEntry> entries) {
        this.entries = entries;
    }
}
