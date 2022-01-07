package xyz.nsgw.tools;

import java.util.Scanner;

public class Reader {
    // Initialize the local Scanner to use for system input.
    private Scanner scanner;
    // Constructor
    public Reader() {
        // Set the local Scanner to a new Scanner.
        this.scanner = new Scanner(System.in);
    }
    // Wait for a line of text (string of) input.
    public String str() {
        return scanner.nextLine();
    }
    // Wait for a valid integer input.
    public int anInt() {
        return scanner.nextInt();
    }
    // Wait for a valid boolean input.
    public boolean bool() {
        return scanner.nextBoolean();
    }
    // Wait for a valid double (decimal) input.
    public double aDouble() {
        return scanner.nextDouble();
    }

}
