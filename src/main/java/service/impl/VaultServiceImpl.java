package service.impl;

import crypto.ICryptoService;
import model.Entry;
import model.Vault;
import service.IVaultService;
import storage.IStorageService;
import storage.IVaultSerializer;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
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
    private static final SecureRandom RANDOM = new SecureRandom();

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
    public void updateEntry(Entry entry) {
        save();
    }

    @Override
    public void deleteEntry(Entry entry) {
        vault.deleteEntry(entry);
        entry.destroy();
        save();
    }

    @Override
    public void changeMasterPassword(char[] currentPassword, char[] newPassword) {
        try {
            if (currentPassword == null || currentPassword.length == 0)
                throw new IllegalArgumentException("Current master password must not be empty");

            if (newPassword == null || newPassword.length < 8)
                throw new IllegalArgumentException("New master password must not be empty");

            if (storage.exists()) {
                byte[] data = storage.load();
                byte[] currentSalt = Arrays.copyOfRange(data, 0, SALT_LENGTH);
                byte[] encrypted = Arrays.copyOfRange(data, SALT_LENGTH, data.length);

                SecretKey currentKey = crypto.deriveKey(currentPassword, currentSalt);
                crypto.decrypt(encrypted, currentKey);
            }

            byte[] newSalt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(newSalt);

            this.salt = newSalt;
            this.key = crypto.deriveKey(newPassword, newSalt);

            save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to change master password", e);
        }
    }

    @Override
    public void backup(Path backupPath) {
        try {
            if (!storage.exists()) save();

            Path parent = backupPath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);

            Files.write(backupPath, storage.load());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create backup", e);
        }
    }

    @Override
    public void restore(Path backupPath, char[] masterPassword) {
        try {
            byte[] data = Files.readAllBytes(backupPath);
            if (data.length <= SALT_LENGTH) throw new IllegalArgumentException("Backup file is too small");

            byte[] backupSalt = Arrays.copyOfRange(data, 0, SALT_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(data, SALT_LENGTH, data.length);

            SecretKey backupKey = crypto.deriveKey(masterPassword, backupSalt);
            byte[] decrypted = crypto.decrypt(encrypted, backupKey);

            Vault restoredVault = serializer.deserialize(decrypted);
            storage.save(data);

            lock();

            this.vault = restoredVault;
            this.key = backupKey;
            this.salt = backupSalt;
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore backup", e);
        }
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
