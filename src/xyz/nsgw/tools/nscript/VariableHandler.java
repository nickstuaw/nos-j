package xyz.nsgw.tools.nscript;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class VariableHandler {

    private Target target;

    private File workingDir;

    private File machineFolder;

    private File logo;

    private String specs;

    private Date time;

    private HashMap<String, String> locals = new HashMap<>();

    public VariableHandler() {}

    public void setTarget(final String target1) {
        switch(target1) {
            case "boot" -> target = Target.BOOT;
            case "shutdown" -> target = Target.SHUTDOWN;
        }
    }

    public Target getTarget() {
        return this.target;
    }

    public void initiateTarget(final String arg) {
        if(target == Target.BOOT) {
            machineFolder = new File(arg);
        }
    }

    public void setVariable(final String var, final String val) {
        switch (var) {
            case "MachineFolder" -> {
                machineFolder = new File(val);
                if(!machineFolder.exists())
                    machineFolder.mkdirs();
            }
            case "Logo" -> logo = new File(machineFolder, "logo.ns");
            case "Specs" -> specs = "WIP";
            case "Time" -> time = new Date();
        }
    }

    public void setLocal(final String id, final String val) {
        locals.put(id, val);
    }

    public String getLocal(final String id) {
        return locals.get(id);
    }

    public String fillIn(String out) {
        out = out.replaceAll("/MachineFolder", machineFolder.getName());
        out = out.replaceAll("/Logo", logo.getAbsolutePath());
        out = out.replaceAll("/Specs", "WIP");
        out = out.replaceAll("/Time", time.toString());
        for(String key : locals.keySet()) {
            out = out.replaceAll("/" + key, locals.get(key));
        }
        return out;
    }

    public String getVariable(final String var) {
        return switch (var) {
            case "MachineFolder" -> machineFolder.getAbsolutePath();
            case "Logo" -> logo.getAbsolutePath();
            case "Specs" -> specs;
            default -> null;
        };
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    public File getWorkingDir() {
        return workingDir;
    }
}
