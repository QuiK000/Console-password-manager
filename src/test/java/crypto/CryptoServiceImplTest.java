package crypto;

import crypto.exception.CryptoException;
import crypto.impl.CryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CryptoServiceImpl")
public class CryptoServiceImplTest {
    private CryptoServiceImpl crypto;
    private byte[] salt;

    @BeforeEach
    void setUp() {
        crypto = new CryptoServiceImpl();
        salt = new byte[16];
        new SecureRandom().nextBytes(salt);
    }

    @Nested
    @DisplayName("deriveKey")
    class DeriveKey {
        @Test
        @DisplayName("Returns the AES key for the correct password and salt")
        void returnsKeyForValidInput() throws NoSuchAlgorithmException, InvalidKeySpecException {
            SecretKey key = crypto.deriveKey("password".toCharArray(), salt);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(32);
        }

        @Test
        @DisplayName("Different passwords produce different keys")
        void differentPasswordsProducesDifferentKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
            SecretKey key1 = crypto.deriveKey("password1".toCharArray(), salt);
            SecretKey key2 = crypto.deriveKey("password2".toCharArray(), salt);
            assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        }

        @Test
        @DisplayName("Different salts produce different keys")
        void differentSaltsProducesDifferentKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] anotherSalt = new byte[16];
            new SecureRandom().nextBytes(anotherSalt);

            SecretKey key1 = crypto.deriveKey("password".toCharArray(), salt);
            SecretKey key2 = crypto.deriveKey("password".toCharArray(), anotherSalt);

            assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        }

        @Test
        @DisplayName("Throws an exception if the salt is null")
        void throwsOnNullSalt() {
            assertThatThrownBy(() -> crypto.deriveKey("password".toCharArray(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Salt");
        }

        @Test
        @DisplayName("Throws an exception if the salt is shorter than 16 bytes")
        void throwsOnShortSalt() {
            assertThatThrownBy(() -> crypto.deriveKey("password".toCharArray(), new byte[8]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("encrypt / decrypt")
    class EncryptDecryptTest {
        private SecretKey key;

        @BeforeEach
        void deriveKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
            key = crypto.deriveKey("masterpassword".toCharArray(), salt);
        }

        @Test
        @DisplayName("Encrypted data can be decrypted back to the original")
        void encryptThenDecryptRoundTrip() throws CryptoException {
            byte[] original = "Hello, World!".getBytes();
            byte[] encrypted = crypto.encrypt(original, key);
            byte[] decrypted = crypto.decrypt(encrypted, key);

            assertThat(decrypted).isEqualTo(original);
        }

        @Test
        @DisplayName("Encrypting the same data produces different results (random IV)")
        void encryptProducesRandomOutput() throws CryptoException {
            byte[] data = "same data".getBytes();
            byte[] enc1 = crypto.encrypt(data, key);
            byte[] enc2 = crypto.encrypt(data, key);

            assertThat(enc1).isNotEqualTo(enc2);
        }

        @Test
        @DisplayName("Encrypts an empty array without exceptions")
        void encryptEmptyBytes() throws CryptoException {
            byte[] encrypted = crypto.encrypt(new byte[0], key);
            byte[] decrypted = crypto.decrypt(encrypted, key);
            assertThat(decrypted).isEmpty();
        }

        @Test
        @DisplayName("Throws an exception when decrypting with an incorrect key")
        void decryptWithWrongKeyThrows() throws CryptoException, NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] data = "secret".getBytes();
            byte[] encrypted = crypto.encrypt(data, key);

            byte[] otherSalt = new byte[16];
            new SecureRandom().nextBytes(otherSalt);

            SecretKey wrongKey = crypto.deriveKey("wrongpassword".toCharArray(), otherSalt);
            assertThatThrownBy(() -> crypto.decrypt(encrypted, wrongKey)).isInstanceOf(CryptoException.class);
        }

        @Test
        @DisplayName("Throws an exception when decrypting corrupted data")
        void decryptCorruptedDataThrows() throws CryptoException {
            byte[] data = "important data".getBytes();
            byte[] encrypted = crypto.encrypt(data, key);

            encrypted[encrypted.length - 1] ^= (byte) 0xFF;
            assertThatThrownBy(() -> crypto.decrypt(encrypted, key)).isInstanceOf(CryptoException.class);
        }

        @Test
        @DisplayName("Encrypt throws an exception for null data")
        void encryptNullThrows() {
            assertThatThrownBy(() -> crypto.encrypt(null, key)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Decrypt throws an exception for data that is too short")
        void decryptTooShortThrows() {
            assertThatThrownBy(() -> crypto.decrypt(new byte[5], key)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
