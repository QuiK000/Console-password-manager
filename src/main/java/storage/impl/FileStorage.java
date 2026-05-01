package storage.impl;

import storage.IStorageService;

import java.nio.file.Path;

public class FileStorage implements IStorageService {
    private Path filePath;

    @Override
    public void save(byte[] data) {

    }

    @Override
    public byte[] load() {
        return new byte[0];
    }

    @Override
    public boolean exists() {
        return false;
    }
}
