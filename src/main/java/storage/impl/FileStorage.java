package storage.impl;

import lombok.AllArgsConstructor;
import storage.IStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@AllArgsConstructor
public class FileStorage implements IStorageService {
    private final Path filePath;

    @Override
    public void save(byte[] data) throws IOException {
        Objects.requireNonNull(data, "data must not be null");
        Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");

        Files.write(
                tempPath,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        Files.move(
                tempPath,
                filePath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
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
