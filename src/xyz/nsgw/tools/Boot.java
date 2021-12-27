package xyz.nsgw.tools;

import java.io.File;

public class Boot {

    public static void checkForLoader(final String directoryPath) {

        File currDir = new File(directoryPath);

        if(currDir.exists()) {
            if(currDir.isDirectory()) {
                currDir = new File(directoryPath, "boot.ns");
            }
        }

    }

}
