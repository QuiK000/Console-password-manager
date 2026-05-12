package util;

import de.vandermeer.asciitable.AsciiTable;
import model.Entry;

import java.util.List;

public class TableUtils {
    public static void printEntries(List<Entry> entries) {
        AsciiTable at = new AsciiTable();

        at.addRule();
        at.addRow("ID", "Site", "Login", "TOTP Code");
        at.addRule();

        int index = 1;
        for (Entry entry : entries) {
            String totp = entry.getTotpSecret() != null ? TotpUtils.generateCode(entry.getTotpSecret()) : "N/A";

            at.addRow(index++, entry.getSite(), entry.getLogin(), totp);
            at.addRule();
        }

        System.out.println(at.render());
    }
}
