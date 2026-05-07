package crypto.impl;

import crypto.ICryptoService;
import crypto.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class CryptoServiceImpl implements ICryptoService {
    private static final int PBKDF2_ITERATIONS = 600_000;
    private static final int AES_KEY_BIT_LENGTH = 256;
    private static final int GCM_TAG_BIT_LENGTH = 128;
    private static final int GCM_IV_BYTE_LENGTH = 12;

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public SecretKey deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (salt == null || salt.length < 16) throw new IllegalArgumentException("Salt must be at least 16 bytes long");

        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, AES_KEY_BIT_LENGTH);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();

            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public byte[] encrypt(byte[] data, SecretKey key) throws CryptoException {
        if (data == null) throw new IllegalArgumentException("Data must not be null");

        try {
            byte[] iv = new byte[GCM_IV_BYTE_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(data);
            byte[] combined = new byte[iv.length + cipherText.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return combined;
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, SecretKey key) throws CryptoException {
        if (data == null || data.length < GCM_IV_BYTE_LENGTH)
            throw new IllegalArgumentException("Invalid encrypted data");

        try {
            byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_BYTE_LENGTH);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(data, GCM_IV_BYTE_LENGTH, data.length - GCM_IV_BYTE_LENGTH);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed (wrong password or corrupted data)", e);
        }
    }
}
