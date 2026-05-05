package service.impl;

import crypto.ICryptoService;
import service.IAuthService;
import storage.IStorageService;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class AuthServiceImpl implements IAuthService {
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ICryptoService crypto;
    private final IStorageService storage;

    private byte[] salt;

    public AuthServiceImpl(ICryptoService crypto, IStorageService storage) {
        this.crypto = crypto;
        this.storage = storage;
    }

    @Override
    public boolean isFirstRun() {
        return !storage.exists();
    }

    @Override
    public SecretKey setupMasterPassword(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return crypto.deriveKey(password, salt);
    }

    @Override
    public SecretKey login(
            char[] password,
            byte[] existingSalt
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.salt = existingSalt;
        return crypto.deriveKey(password, salt);
    }
}
