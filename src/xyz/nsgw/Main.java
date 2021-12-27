package xyz.nsgw;

import xyz.nsgw.tools.Reader;
import xyz.nsgw.tools.nscript.Runner;

import java.io.File;

public class Main {

    private static final Reader SYS_READER = new Reader();

    public static void main(String[] args) {
        // Boot
        boot();
    }

    public static void boot() {
        // Startup symbol
        // Look for bootloader in working directory
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
        Runner runtime = new Runner(bootloader);
        runtime.run();
    }

}
