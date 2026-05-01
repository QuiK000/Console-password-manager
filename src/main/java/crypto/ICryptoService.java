package crypto;

import javax.crypto.SecretKey;

public interface ICryptoService {
    SecretKey deriveKey(char[] password, byte[] salt);

    byte[] encrypt(byte[] data, SecretKey key);

    byte[] decrypt(byte[] data, SecretKey key);
}
