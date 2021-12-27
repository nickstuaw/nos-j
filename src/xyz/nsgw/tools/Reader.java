package xyz.nsgw.tools;

import java.util.Scanner;

public class Reader {

    private Scanner scanner;

    public Reader() {
        this.scanner = new Scanner(System.in);
    }

    public String str() {
        return scanner.nextLine();
    }

    public int anInt() {
        return scanner.nextInt();
    }

    public boolean bool() {
        return scanner.nextBoolean();
    }

    public double aDouble() {
        return scanner.nextDouble();
    }

}
