package xyz.nsgw;

import xyz.nsgw.tools.Reader;
import xyz.nsgw.tools.nscript.ScriptRunHandler;

import java.io.File;

public class Main {

    private static final ScriptRunHandler queue = new ScriptRunHandler();

    private static final Reader SYS_READER = new Reader();

    private static File folder, userFolder;

    public static void main(String[] args) {
        //Boot
        boot();
    }

    public static void boot() {
        //todo: Show startup symbol
        //Look for bootloader in working directory
        File bootloader = new File("boot.ns");
        while(!bootloader.exists()) {
            System.out.println("No bootloader was found! Where should I boot from?");
            bootloader = new File(SYS_READER.str());
            if(!bootloader.exists()) {
                System.out.println("Not found.");
            } else if(!bootloader.getName().endsWith(".ns") || !bootloader.isFile()) {
                System.out.println("Invalid bootloader!");
            }
        }
        //Run the bootloader script
        queue.start();
        queue.queue(bootloader);
    }

    public static ScriptRunHandler getScriptHandler() {
        return queue;
    }

    public static File getFolder() {
        return folder;
    }

    public static void setFolder(File folder) {
        Main.folder = folder;
    }

    public static File getUserFolder() {
        return userFolder;
    }

    public static void setUserFolder(File folder) {
        Main.userFolder = folder;
    }
}
