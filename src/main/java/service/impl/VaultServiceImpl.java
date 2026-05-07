package service.impl;

import crypto.ICryptoService;
import model.Entry;
import model.Vault;
import service.IVaultService;
import storage.IStorageService;
import storage.IVaultSerializer;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

public class VaultServiceImpl implements IVaultService {
    private Vault vault;

    private final IStorageService storage;
    private final IVaultSerializer serializer;
    private final ICryptoService crypto;

    private SecretKey key;
    private byte[] salt;

    private static final int SALT_LENGTH = 16;

    public VaultServiceImpl(IStorageService storage, IVaultSerializer serializer, ICryptoService crypto) {
        this.storage = storage;
        this.serializer = serializer;
        this.crypto = crypto;
    }

    public void setSecurity(SecretKey key, byte[] salt) {
        this.key = key;
        this.salt = salt;
    }

    @Override
    public void init() {
        try {
            if (storage.exists()) {
                byte[] data = storage.load();
                salt = Arrays.copyOfRange(data, 0, SALT_LENGTH);

                byte[] encrypted = Arrays.copyOfRange(data, SALT_LENGTH, data.length);
                byte[] decrypted = crypto.decrypt(encrypted, key);

                vault = serializer.deserialize(decrypted);
                System.out.println("Vault unlocked");
            } else {
                vault = new Vault();
                System.out.println("New vault created");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init value (wrong password or corrupted file)", e);
        }
    }

    @Override
    public void lock() {
        if (vault != null) vault.clearAll();
        vault = null;
        key = null;
        salt = null;
    }

    @Override
    public boolean isLocked() {
        return vault == null;
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
    public void updateEntry(Entry entry) {
        save();
    }

    @Override
    public void deleteEntry(Entry entry) {
        vault.deleteEntry(entry);
        entry.destroy();
        save();
    }

    private void save() {
        try {
            byte[] serialized = serializer.serialize(vault);
            byte[] encrypted = crypto.encrypt(serialized, key);
            byte[] finalData = new byte[salt.length + encrypted.length];

            System.arraycopy(salt, 0, finalData, 0, salt.length);
            System.arraycopy(encrypted, 0, finalData, salt.length, encrypted.length);

            storage.save(finalData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save vault", e);
        }
    }
}
