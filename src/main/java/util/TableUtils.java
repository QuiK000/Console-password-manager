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
            String site = entry.getSite() != null ? entry.getSite() : "N/A";
            String login = entry.getLogin() != null ? entry.getLogin() : "N/A";
            String totp = entry.getTotpSecret() != null ? TotpUtils.generateCode(entry.getTotpSecret()) : "N/A";
            String notes = entry.getNotes() != null ? entry.getNotes() : "N/A";

            int index = sourceEntries.indexOf(entry) + 1;

            at.addRow(index, site, login, totp, notes);
            at.addRule();
        }

        System.out.println(at.render());
    }
}
