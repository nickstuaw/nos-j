package xyz.nsgw.tools.nscript;

import java.io.File;

public class Service {

    private final int lockId;

    private boolean locked = false;

    public Service(final int id) {
        this.lockId = id;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isUnlocked() {
        return !locked;
    }

    public void startScript(final File file) {
        (new Runner(file, lockId)).start();
    }

}
