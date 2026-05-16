package storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import storage.impl.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileStorage")
public class FileStorageTest {
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private Path vaultFile;

    @BeforeEach
    void setUp() {
        vaultFile = tempDir.resolve("vault.json");
        storage = new FileStorage(vaultFile);
    }

    @Test
    @DisplayName("Exists returns false if the file does not exist")
    void existsReturnsFalseWhenFileAbsent() {
        assertThat(storage.exists()).isFalse();
    }

    @Test
    @DisplayName("Exists returns treu after saving")
    void existsReturnsTrueAfterSave() throws IOException {
        storage.save(new byte[]{1, 2, 3});
        assertThat(storage.exists()).isTrue();
    }

    @Test
    @DisplayName("Save and load return the same data")
    void saveAndLoadRoundTrip() throws IOException {
        byte[] data = "encrypted vault content".getBytes();
        storage.save(data);
        assertThat(storage.load()).isEqualTo(data);
    }

    @Test
    @DisplayName("Save overwrites the prevoius data")
    void saveOverwritesPreviousData() throws IOException {
        storage.save("first".getBytes());
        storage.save("second".getBytes());
        assertThat(storage.load()).isEqualTo("second".getBytes());
    }

    @Test
    @DisplayName("Load returns an empty array if the file does not exist")
    void loadReturnEmptyArrayWhenFileAbsent() throws IOException {
        assertThat(storage.load()).isEmpty();
    }

    @Test
    @DisplayName("Save does not leave a temporary file after writing")
    void noTempFileAfterSave() throws IOException {
        storage.save("data".getBytes());
        Path tempFile = vaultFile.resolveSibling(vaultFile.getFileName() + ".tmp");
        assertThat(Files.exists(tempFile)).isFalse();
    }

    @Test
    @DisplayName("Save throws an exception when the data is null")
    void saveThrowsOnNull() {
        assertThatThrownBy(() -> storage.save(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Saves a large amount of data without errors")
    void saveLargeData() throws IOException {
        byte[] largeData = new byte[1024 * 1024];
        new SecureRandom().nextBytes(largeData);
        storage.save(largeData);
        assertThat(storage.load()).isEqualTo(largeData);
    }
}
