package service;

import crypto.ICryptoService;
import crypto.exception.CryptoException;
import model.Entry;
import model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.impl.VaultServiceImpl;
import storage.IStorageService;
import storage.IVaultSerializer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VaultServiceImpl")
public class VaultServiceImplTest {
    @Mock
    private IStorageService storage;

    @Mock
    private IVaultSerializer serializer;

    @Mock
    private ICryptoService crypto;

    private VaultServiceImpl service;
    private SecretKey key;
    private byte[] salt;

    @BeforeEach
    void setUp() {
        service = new VaultServiceImpl(storage, serializer, crypto);
        salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        key = new SecretKeySpec(new byte[32], "AES");
        service.setSecurity(key, salt);
    }

    private void initEmptyVault() {
        when(storage.exists()).thenReturn(false);
        service.init();
    }

    private void initVaultWithData(Vault vault) throws IOException, CryptoException {
        byte[] fullData = new byte[salt.length + 28];
        System.arraycopy(salt, 0, fullData, 0, salt.length);

        when(storage.exists()).thenReturn(true);
        when(storage.load()).thenReturn(fullData);
        when(crypto.decrypt(any(), eq(key))).thenReturn(new byte[0]);
        when(serializer.deserialize(any())).thenReturn(vault);

        service.init();
    }

    private Entry makeEntry(String site, String login) {
        return Entry.builder()
                .id(UUID.randomUUID().toString())
                .site(site)
                .login(login)
                .password("pass".toCharArray())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("init")
    class InitTest {
        @Test
        @DisplayName("Creates a new vault on first launch")
        void createsNewVaultOnFirstRun() {
            when(storage.exists()).thenReturn(false);
            service.init();

            assertThat(service.isLocked()).isFalse();
            assertThat(service.listEntries()).isEmpty();
        }

        @Test
        @DisplayName("Loads an existing vault from storage")
        void loadsExistingVault() throws IOException, CryptoException {
            Vault vault = new Vault();
            vault.addEntry(makeEntry("github.com", "user@example.com"));

            initVaultWithData(vault);
            assertThat(service.listEntries()).hasSize(1);
        }

        @Test
        @DisplayName("Throws a RuntimeException for an incorrect password")
        void throwsOnDecryptionFailure() throws IOException, CryptoException {
            byte[] fullData = new byte[salt.length + 28];
            System.arraycopy(salt, 0, fullData, 0, salt.length);

            when(storage.exists()).thenReturn(true);
            when(storage.load()).thenReturn(fullData);
            when(crypto.decrypt(any(), any())).thenThrow(new RuntimeException("Bad decrypt"));

            assertThatThrownBy(() -> service.init())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to init");
        }
    }

    @Nested
    @DisplayName("lock / isLocked")
    class LockTest {
        @Test
        @DisplayName("isLocked returns true after lock()")
        void isLockedAfterLock() {
            initEmptyVault();
            assertThat(service.isLocked()).isFalse();

            service.lock();
            assertThat(service.isLocked()).isTrue();
        }

        @Test
        @DisplayName("isLocked returns true before init()")
        void isLockedBeforeInit() {
            assertThat(service.isLocked()).isTrue();
        }

        @Test
        @DisplayName("listEntries throws a NullPointerException after lock()")
        void listEntriesAfterLockThrows() {
            initEmptyVault();
            service.lock();
            assertThatThrownBy(() -> service.listEntries()).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("addEntry / listEntries")
    class AddListTest {
        @BeforeEach
        void init() throws CryptoException {
            initEmptyVault();
            when(serializer.serialize(any())).thenReturn(new byte[0]);
            when(crypto.encrypt(any(), any())).thenReturn(new byte[28]);
        }

        @Test
        @DisplayName("The added entry appears in the list")
        void addedEntryAppearsInList() {
            Entry entry = makeEntry("github.com", "dev@test.com");
            service.addEntry(entry);

            List<Entry> entries = service.listEntries();
            assertThat(entries).hasSize(1);
            assertThat(entries.getFirst().getSite()).isEqualTo("github.com");
        }

        @Test
        @DisplayName("addEntry triggers saving")
        void addEntryTriggersSave() throws IOException {
            service.addEntry(makeEntry("site.com", "user"));
            verify(storage).save(any());
        }

        @Test
        @DisplayName("Multiple entries can be added")
        void canAddMultipleEntries() {
            service.addEntry(makeEntry("site1.com", "a"));
            service.addEntry(makeEntry("site2.com", "b"));
            service.addEntry(makeEntry("site3.com", "c"));

            assertThat(service.listEntries()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("deleteEntry")
    class DeleteTest {
        @BeforeEach
        void init() throws CryptoException {
            initEmptyVault();
            when(serializer.serialize(any())).thenReturn(new byte[0]);
            when(crypto.encrypt(any(), any())).thenReturn(new byte[28]);
        }

        @Test
        @DisplayName("The removed entry disappears from the list")
        void deleteEntryRemovedFromList() {
            Entry entry = makeEntry("github.com", "user");

            service.addEntry(entry);
            service.deleteEntry(entry);

            assertThat(service.listEntries()).isEmpty();
        }

        @Test
        @DisplayName("deleteEntry triggers saving")
        void deleteEntryTriggersSave() throws IOException {
            Entry entry = makeEntry("github.com", "user");
            service.addEntry(entry);

            clearInvocations(storage);
            service.deleteEntry(entry);

            verify(storage).save(any());
        }
    }

    @Nested
    @DisplayName("updateEntry")
    class UpdateTest {
        @BeforeEach
        void init() throws CryptoException {
            initEmptyVault();
            when(serializer.serialize(any())).thenReturn(new byte[0]);
            when(crypto.encrypt(any(), any())).thenReturn(new byte[28]);
        }

        @Test
        @DisplayName("updateEntry triggers saving")
        void updateEntryTriggersSave() throws IOException {
            Entry entry = makeEntry("site.com", "user");
            service.addEntry(entry);

            clearInvocations(storage);
            entry.setSite("new-site.com");

            service.updateEntry(entry);
            verify(storage).save(any());
        }

        @Test
        @DisplayName("Changes to an entry are reflected in subsequent listEntries calls")
        void changesAreVisible() {
            Entry entry = makeEntry("old.com", "user");
            service.addEntry(entry);

            entry.setSite("new.com");
            service.updateEntry(entry);

            assertThat(service.listEntries().getFirst().getSite()).isEqualTo("new.com");
        }
    }

    @Nested
    @DisplayName("changeMasterPassword")
    class ChangeMasterPasswordTest {
        @BeforeEach
        void init() {
            initEmptyVault();
        }

        @Test
        @DisplayName("Throws an exception when the current password is empty")
        void throwsOnEmptyCurrentPassword() {
            assertThatThrownBy(() -> service.changeMasterPassword(new char[0], "newpass".toCharArray()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Throws an exception if the new password is shorter than 8 characters")
        void throwsOnShortNewPassword() {
            assertThatThrownBy(() -> service.changeMasterPassword("current".toCharArray(), "short".toCharArray()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Successfully changes the password when the vault does not exist in storage")
        void successWhenVaultDoesNotExistOnDisk() throws CryptoException, NoSuchAlgorithmException, InvalidKeySpecException {
            when(storage.exists()).thenReturn(false);
            when(serializer.serialize(any())).thenReturn(new byte[0]);
            when(crypto.encrypt(any(), any())).thenReturn(new byte[28]);
            when(crypto.deriveKey(any(), any())).thenReturn(key);

            assertThatCode(() ->
                    service.changeMasterPassword("currentpass".toCharArray(), "newpassword".toCharArray())
            ).doesNotThrowAnyException();
        }
    }
}
