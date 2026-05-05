package crypto;

import crypto.exception.CryptoException;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface ICryptoService {
    SecretKey deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException;

    byte[] encrypt(byte[] data, SecretKey key) throws CryptoException;

    byte[] decrypt(byte[] data, SecretKey key) throws CryptoException;
}
