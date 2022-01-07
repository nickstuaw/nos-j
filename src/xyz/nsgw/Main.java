package xyz.nsgw;

import xyz.nsgw.tools.Reader;
import xyz.nsgw.tools.nscript.ScriptRunHandler;

import java.io.File;

public class Main {

    // Initialize & set
    // 1. the handler to queue & run scripts.
    private static final ScriptRunHandler queue = new ScriptRunHandler();
    // 2. the Reader to take user input from the console.
    private static final Reader SYS_READER = new Reader();
    // Initialize the File objects to store the active directories in.
    private static File folder, userFolder;
    // Main method. The program starts here.
    public static void main(String[] args) {
        // "Boot" (switch on) the machine.
        boot();
    }
    // boot() creates and runs the N.OS machine.
    public static void boot() {
        //todo: Show startup symbol
        // Initialize & set the bootloader file.
        File bootloader = new File("boot.ns");
        // Check whether bootloader exists.
        while(!bootloader.exists()) {
            // If the file doesn't exist, ask for the path again.
            System.out.println("No bootloader was found! Where should I boot from?");
            // Take a line of user input as the path.
            bootloader = new File(SYS_READER.str());
            // If the file is not found or is invalid, continue.
            if(!bootloader.exists()) {
                // Output an error message.
                System.out.println("Not found.");
            } else if(!bootloader.getName().endsWith(".ns") || !bootloader.isFile()) {
                // File requirements:
                // - It must include the ".ns" file extension.
                // - It must be a file if it exists.
                // Output an error message.
                System.out.println("Invalid bootloader!");
            }
        }
        // Start listening for scripts that are added to the queue.
        queue.start();
        // Start the script within the bootloader file.
        queue.queue(bootloader);
    }
    // Get the script handler.
    public static ScriptRunHandler getScriptHandler() {
        // Return the script handler.
        return queue;
    }
    // Get the machine folder that is set globally.
    public static File getFolder() {
        return folder;
    }
    // Set the globally recognised machine folder to a valid directory.
    public static void setFolder(File folder) {
        Main.folder = folder;
    }
    // Get the folder containing users' folders.
    public static File getUserFolder() {
        return userFolder;
    }
    // Set the globally recognised user folder.
    public static void setUserFolder(File folder) {
        Main.userFolder = folder;
    }
}
