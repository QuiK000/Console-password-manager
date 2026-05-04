package service;

import model.Entry;

import java.util.List;

public interface IVaultService {
    void init();

    void addEntry(Entry entry);

    List<Entry> listEntries();

    Entry getEntry(Entry entry);

    void deleteEntry(Entry entry);
}
