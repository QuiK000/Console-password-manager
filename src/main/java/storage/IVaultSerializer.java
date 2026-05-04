package storage;

import model.Vault;

public interface IVaultSerializer {
    byte[] serialize(Vault vault);

    Vault deserialize(byte[] data);
}
