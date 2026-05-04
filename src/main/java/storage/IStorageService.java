package storage;

import java.io.IOException;

public interface IStorageService {
    void save(byte[] data) throws IOException;

    byte[] load() throws IOException;

    boolean exists();
}
