package model;

import java.util.ArrayList;
import java.util.List;

public class Vault {
    private final List<Entry> entries = new ArrayList<>();

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries.stream().toList();
    }
}
