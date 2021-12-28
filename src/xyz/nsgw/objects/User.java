package xyz.nsgw.objects;

public class User extends UserFileManager {

    private String name;

    public User(final String name) {
        this.name = name;
        setHomeName(this.name);
        if(!ensureHomeExistence()) {
            System.out.println("An error occurred: home directory could not be created. Please make it manually.");
        }
    }

    public void renameTo(final String name) {
        this.name = name;
        if(!renameHome(name)) {
            System.out.println("An error occurred: the home directory could not be renamed. Please rename manually.");
        }
    }

}
