package service;

import model.Entry;

import java.nio.file.Path;
import java.util.List;

public interface IVaultService {
    void init();

    void lock();

    boolean isLocked();

    void addEntry(Entry entry);

    List<Entry> listEntries();

    void updateEntry(Entry entry);

    void deleteEntry(Entry entry);

    void changeMasterPassword(char[] currentPassword, char[] newPassword);

    void backup(Path backupPath);

    void restore(Path backupPath, char[] masterPassword);
}
