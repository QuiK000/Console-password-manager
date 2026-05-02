package service.impl;

import lombok.AllArgsConstructor;
import model.Entry;
import model.Vault;
import service.IVaultService;

import java.util.List;

@AllArgsConstructor
public class VaultServiceImpl implements IVaultService {
    private final Vault vault;

    @Override
    public void addEntry(Entry entry) {
        vault.addEntry(entry);
    }

    @Override
    public List<Entry> listEntries() {
        return vault.getEntries();
    }

    @Override
    public Entry getEntry(String id) {
        return null;
    }

    @Override
    public void deleteEntry(String id) {

    }
}
