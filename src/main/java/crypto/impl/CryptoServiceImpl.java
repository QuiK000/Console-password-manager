package crypto.impl;

import crypto.ICryptoService;

import javax.crypto.SecretKey;

public class CryptoServiceImpl implements ICryptoService {
    @Override
    public SecretKey deriveKey(char[] password, byte[] salt) {
        return null;
    }

    @Override
    public byte[] encrypt(byte[] data, SecretKey key) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data, SecretKey key) {
        return new byte[0];
    }
}
