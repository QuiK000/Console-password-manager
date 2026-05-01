package storage;

public interface IStorageService {
    void save(byte[] data);

    byte[] load();

    boolean exists();
}
