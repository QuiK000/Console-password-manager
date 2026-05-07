package util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ConsoleUtils {
    public static final Scanner SCANNER = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    public static char[] readPassword(String prompt) {
        if (System.console() != null) return System.console().readPassword(prompt);
        System.out.print(prompt);
        return SCANNER.nextLine().toCharArray();
    }

    public static void copyToClipboard(String text) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);

            System.out.println("(Copied to clipboard. Will be cleared in 15 seconds)");
            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        clipboard.setContents(new StringSelection(" "), null);
                    } catch (Exception ignored) {}
                }
            }, 15000);
        } catch (Exception e) {
            System.out.println("(Clipboard not available)");
        }
    }

    public static String mask(String value) {
        if (value == null || value.isEmpty()) return "";

        int visible = Math.min(2, value.length());
        String visiblePart = value.substring(0, visible);

        return visiblePart + "*".repeat(value.length() - visible);
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
