package xyz.nsgw.tools.nscript;

import xyz.nsgw.Main;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class VariableHandler {

    private Target target;

    private File workingDir;

    private File scriptFolder;

    private File logo;

    private Date time;

    private HashMap<String, String> locals = new HashMap<>();

    public VariableHandler() {}

    public void reset() {
        locals = new HashMap<>();
    }

    public void setTarget(final String target1) {
        switch(target1) {
            case "boot" -> target = Target.BOOT;
            case "shutdown" -> target = Target.SHUTDOWN;
        }
    }

    public void initiateTarget(final String arg) {
        if(target == Target.BOOT) {
            Main.setFolder(new File(arg));
        }
    }

    public boolean setVariable(final String var, final String val) {
        switch (var) {
            case "MachineFolder" -> {
                Main.setFolder(new File(val));
                if(!Main.getFolder().exists())
                    return Main.getFolder().mkdirs();
            }
            case "UserFolder" -> {
                Main.setUserFolder(new File(Main.getFolder(), val));
                if(!Main.getUserFolder().exists())
                    return Main.getUserFolder().mkdirs();
            }
            case "ScriptFolder" -> {
                scriptFolder = new File(Main.getFolder(), val);
                if(!scriptFolder.exists())
                    return scriptFolder.mkdirs();
            }
            case "Logo" -> logo = new File(Main.getFolder(), "logo.ns");
            case "Time" -> time = new Date();
        }
        return true;
    }

    public void setLocal(final Reference reference) {
        locals.put(reference.getIdentifier(), reference.getValue());
    }

    public void setLocal(final String id, final String val) {
        locals.put(id, val);
    }

    public String getLocal(final String id) {
        return locals.get(id);
    }

    public String fillIn(String out) {
        out = out.replaceAll("/MachineFolder", Main.getFolder().getName());
        out = out.replaceAll("/UserFolder", Main.getUserFolder().getName());
        out = out.replaceAll("/ScriptFolder", Main.getUserFolder().getName());
        out = out.replaceAll("/Logo", logo.getName());
        out = out.replaceAll("/Time", time.toString());
        for(String key : locals.keySet()) {
            out = out.replaceAll("/" + key, locals.get(key));
        }
        return out;
    }

    public File getMachineFolder() {
        return Main.getFolder();
    }

    public File getScriptFolder() {
        return scriptFolder;
    }

    public File getUserFolder() {
        return Main.getUserFolder();
    }

    public File getLogo() {
        return logo;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    public File getWorkingDir() {
        return workingDir;
    }
}
