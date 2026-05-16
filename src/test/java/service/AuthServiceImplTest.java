package service;

import crypto.ICryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.impl.AuthServiceImpl;
import storage.IStorageService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
public class AuthServiceImplTest {
    @Mock
    private ICryptoService crypto;

    @Mock
    private IStorageService storage;

    private AuthServiceImpl auth;
    private SecretKey fakeKey;
    private byte[] fakeSalt;

    @BeforeEach
    void setUp() {
        auth = new AuthServiceImpl(crypto, storage);
        fakeSalt = new byte[16];
        new SecureRandom().nextBytes(fakeSalt);
        fakeKey = new SecretKeySpec(new byte[32], "AES");
    }

    @Test
    @DisplayName("isFirstRun returns true if the storage does not exist")
    void isFirstRunWhenStorageDoesNotExist() {
        when(storage.exists()).thenReturn(false);
        assertThat(auth.isFirstRun()).isTrue();
    }

    @Test
    @DisplayName("isFirstRun returns false if the storage exists")
    void isNotFirstRunWhenStorageExists() {
        when(storage.exists()).thenReturn(true);
        assertThat(auth.isFirstRun()).isFalse();
    }

    @Test
    @DisplayName("setupMasterPassword generates a salt and returns a key")
    void setupMasterPasswordGeneratesSaltAndKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] password = "master".toCharArray();
        when(crypto.deriveKey(eq(password), any(byte[].class))).thenReturn(fakeKey);
        SecretKey result = auth.setupMasterPassword(password);

        assertThat(result).isEqualTo(fakeKey);
        assertThat(auth.getSalt()).isNotNull().hasSize(16);
        verify(crypto).deriveKey(eq(password), any(byte[].class));
    }

    @Test
    @DisplayName("setupMasterPassword generates a new salt on each call")
    void setupMasterPasswordGeneratesUniqueSaltEachTime() throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] password = "master".toCharArray();
        when(crypto.deriveKey(any(), any())).thenReturn(fakeKey);

        auth.setupMasterPassword(password);
        byte[] salt1 = auth.getSalt().clone();

        auth.setupMasterPassword(password);
        byte[] salt2 = auth.getSalt().clone();

        assertThat(salt1).isNotEqualTo(salt2);
    }

    @Test
    @DisplayName("login uses the provided salt and returns a key")
    void loginUsesProvidedSalt() throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] password = "master".toCharArray();
        when(crypto.deriveKey(password, fakeSalt)).thenReturn(fakeKey);

        SecretKey result = auth.login(password, fakeSalt);

        assertThat(result).isEqualTo(fakeKey);
        assertThat(auth.getSalt()).isEqualTo(fakeSalt);
        verify(crypto).deriveKey(password, fakeSalt);
    }

    @Test
    @DisplayName("getSalt returns null before the first call to setup or login")
    void getSaltIsNullInitially() {
        assertThat(auth.getSalt()).isNull();
    }
}
