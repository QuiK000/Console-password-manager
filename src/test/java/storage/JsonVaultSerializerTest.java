package storage;

import model.Entry;
import model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import storage.impl.JsonVaultSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonVaultSerializer")
public class JsonVaultSerializerTest {
    private JsonVaultSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonVaultSerializer();
    }

    private Entry makeEntry(String site, String login, String password) {
        return Entry.builder()
                .id(UUID.randomUUID().toString())
                .site(site)
                .login(login)
                .password(password.toCharArray())
                .createdAt(LocalDateTime.of(2026, 6, 15, 12, 0))
                .build();
    }

    @Test
    @DisplayName("An empty vault is serialized and deserialized co")
    void emptyVaultRoundTrip() {
        Vault vault = new Vault();
        byte[] bytes = serializer.serialize(vault);

        Vault result = serializer.deserialize(bytes);
        assertThat(result.getEntries()).isEmpty();
    }

    @Test
    @DisplayName("A vault with entries is serialized and deserialized correctly")
    void vaultWithEntriesRoundTrip() {
        Vault vault = new Vault();

        vault.addEntry(makeEntry("github.com", "dev@test.com", "p@ssw0rd"));
        vault.addEntry(makeEntry("google.com", "user@gmail.com", "hunter2"));

        byte[] bytes = serializer.serialize(vault);
        Vault result = serializer.deserialize(bytes);

        assertThat(result.getEntries()).hasSize(2);
        assertThat(result.getEntries().getFirst().getSite()).isEqualTo("github.com");
        assertThat(result.getEntries().getLast().getLogin()).isEqualTo("user@gmail.com");
    }

    @Test
    @DisplayName("The password is stored correctly as a char[].")
    void passwordPreservedAsCharArray() {
        Vault vault = new Vault();
        vault.addEntry(makeEntry("site.com", "user", "s3cr3tP@ss"));

        byte[] bytes = serializer.serialize(vault);
        Vault result = serializer.deserialize(bytes);

        assertThat(new String(result.getEntries().getFirst().getPassword())).isEqualTo("s3cr3tP@ss");
    }

    @Test
    @DisplayName("notes and totpSecret are passed correctly (nullable fields)")
    void nullableFieldsPreserved() {
        Vault vault = new Vault();
        Entry withNotes = Entry.builder()
                .id(UUID.randomUUID().toString())
                .site("example.com")
                .login("user")
                .password("pass".toCharArray())
                .notes("important notes")
                .totpSecret("JBSWY3DPEHPK3PXP")
                .createdAt(LocalDateTime.now())
                .build();

        Entry withoutNotes = makeEntry("bare.com", "user2", "pass2");

        vault.addEntry(withNotes);
        vault.addEntry(withoutNotes);

        byte[] bytes = serializer.serialize(vault);
        Vault result = serializer.deserialize(bytes);

        assertThat(result.getEntries().getFirst().getNotes()).isEqualTo("important notes");
        assertThat(result.getEntries().getFirst().getTotpSecret()).isEqualTo("JBSWY3DPEHPK3PXP");
        assertThat(result.getEntries().getLast().getNotes()).isNull();
        assertThat(result.getEntries().getLast().getTotpSecret()).isNull();
    }

    @Test
    @DisplayName("Password history is serialized correctly")
    void passwordHistoryPreserved() {
        Vault vault = new Vault();
        Entry entry = makeEntry("site.com", "user", "newpass");

        entry.pushToHistory("oldpass1".toCharArray());
        entry.pushToHistory("oldpass2".toCharArray());

        vault.addEntry(entry);
        byte[] bytes = serializer.serialize(vault);

        Vault result = serializer.deserialize(bytes);
        Entry restored = result.getEntries().getFirst();

        assertThat(restored.getHistory()).hasSize(2);
    }

    @Test
    @DisplayName("Deserializing null bytes returns an empty vault")
    void deserializeNullReturnsEmptyVault() {
        Vault result = serializer.deserialize(null);
        assertThat(result.getEntries()).isEmpty();
    }

    @Test
    @DisplayName("Deserializing an empty array returns an empty vault")
    void deserializeEmptyArrayReturnsEmptyVault() {
        Vault result = serializer.deserialize(new byte[0]);
        assertThat(result.getEntries()).isEmpty();
    }
}
