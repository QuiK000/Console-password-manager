package service.impl;

import lombok.AllArgsConstructor;
import model.Entry;
import model.Vault;
import service.IVaultService;
import storage.IStorageService;
import storage.IVaultSerializer;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class VaultServiceImpl implements IVaultService {
    private Vault vault;

    private final IStorageService storage;
    private final IVaultSerializer serializer;

    @Override
    public void init() {
        try {
            if (storage.exists()) {
                byte[] data = storage.load();
                vault = serializer.deserialize(data);
            } else {
                vault = new Vault();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load vault", e);
        }
    }

    @Override
    public void addEntry(Entry entry) {
        vault.addEntry(entry);
        save();
    }

    @Override
    public List<Entry> listEntries() {
        return vault.getEntries();
    }

    @Override
    public Entry getEntry(Entry entry) {
        return entry;
    }

    @Override
    public void deleteEntry(Entry entry) {
        vault.deleteEntry(entry);
        save();
    }

    private void save() {
        try {
            byte[] data = serializer.serialize(vault);
            storage.save(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save vault", e);
        }
    }
}
