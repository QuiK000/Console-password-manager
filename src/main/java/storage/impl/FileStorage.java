package storage.impl;

import lombok.AllArgsConstructor;
import storage.IStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@AllArgsConstructor
public class FileStorage implements IStorageService {
    private final Path filePath;

    @Override
    public void save(byte[] data) throws IOException {
        Objects.requireNonNull(data, "data must not be null");
        Files.write(
                filePath,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public byte[] load() throws IOException {
        if (!exists()) return new byte[0];
        return Files.readAllBytes(filePath);
    }

    @Override
    public boolean exists() {
        return Files.exists(filePath);
    }
}
