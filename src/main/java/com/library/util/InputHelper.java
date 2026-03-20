package com.library.util;

import java.util.Scanner;

public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    public static boolean readBoolean(String prompt) {
        String input = readString(prompt + " (y/n): ").toLowerCase();
        return input.equals("y") || input.equals("yes") || input.equals("д") || input.equals("да");
    }

    public static void close() {
        scanner.close();
    }
}