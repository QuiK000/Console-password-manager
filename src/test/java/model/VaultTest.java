package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vault")
public class VaultTest {
    @Test
    @DisplayName("A new vault is empty")
    void newVaultIsEmpty() {
        assertThat(new Vault().getEntries()).isEmpty();
    }

    @Test
    @DisplayName("addEntry adds an entry to the vault")
    void addEntry() {
        Vault vault = new Vault();
        Entry entry = Entry.builder()
                .id(UUID.randomUUID().toString())
                .site("site")
                .login("login")
                .password(new char[0])
                .createdAt(LocalDateTime.now())
                .build();

        vault.addEntry(entry);
        assertThat(vault.getEntries()).hasSize(1);
    }

    @Test
    @DisplayName("deleteEntry removes an entry from the vault")
    void deleteEntry() {
        Vault vault = new Vault();
        Entry entry = Entry.builder()
                .id(UUID.randomUUID().toString())
                .site("site")
                .login("login")
                .password(new char[0])
                .createdAt(LocalDateTime.now())
                .build();

        vault.addEntry(entry);
        vault.deleteEntry(entry);

        assertThat(vault.getEntries()).isEmpty();
    }

    @Test
    @DisplayName("getEntries returns an immutable list")
    void getEntriesIsImmutable() {
        Vault vault = new Vault();
        assertThatThrownBy(() -> vault.getEntries().add(
                Entry.builder()
                        .id("x")
                        .site("s")
                        .login("l")
                        .password(new char[0])
                        .createdAt(LocalDateTime.now())
                        .build()
        )).isInstanceOf(UnsupportedOperationException.class);
    }
}
