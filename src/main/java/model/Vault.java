package model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Vault {
    private final List<Entry> entries = new ArrayList<>();

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries.stream().toList();
    }

    public void deleteEntry(Entry entry) {
        entries.remove(entry);
    }

    public void clearAll() {
        for (Entry entry : entries) {
            entry.destroy();
        }
    }
}
