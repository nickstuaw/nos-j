package xyz.nsgw.objects.user;

import xyz.nsgw.Main;

import java.io.File;

public class UserFileManager {

    protected File home;

    protected void setHomeName(final String name) {
        home = new File(Main.getUserFolder(), name);
    }

    protected void ensureHomeExistence() {
        if(!home.exists()) {
            home.mkdirs();
        }
    }

    protected boolean renameHome(final String newName) {
        return home.renameTo(new File(newName));
    }

}
