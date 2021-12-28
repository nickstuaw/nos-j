package xyz.nsgw.objects;

import java.io.File;

public class UserFileManager {

    protected File home;

    protected void setHomeName(final String name) {
        home = new File(name);
    }

    protected boolean ensureHomeExistence() {
        if(!home.exists()) {
            return home.mkdirs();
        }
        return false;
    }

    protected boolean renameHome(final String newName) {
        return home.renameTo(new File(newName));
    }

}
