package util;

import de.vandermeer.asciitable.AsciiTable;
import model.Entry;

import java.util.List;

public class TableUtils {
    public static void printEntries(List<Entry> entries, List<Entry> sourceEntries) {
        AsciiTable at = new AsciiTable();

        at.addRule();
        at.addRow("ID", "Site", "Login", "TOTP Code", "Notes");
        at.addRule();

        for (Entry entry : entries) {
            String totp = entry.getTotpSecret() != null ? TotpUtils.generateCode(entry.getTotpSecret()) : "N/A";
            int index = sourceEntries.indexOf(entry) + 1;

            at.addRow(index, entry.getSite(), entry.getLogin(), totp, entry.getNotes());
            at.addRule();
        }

        System.out.println(at.render());
    }
}
