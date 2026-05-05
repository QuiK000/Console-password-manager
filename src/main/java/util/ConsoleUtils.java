package util;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static char[] readPassword(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().toCharArray();
    }

    public static void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(text), null);

            System.out.println("(Copied to clipboard)");
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
}
