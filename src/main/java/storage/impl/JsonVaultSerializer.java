package storage.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Vault;
import storage.IVaultSerializer;

import java.io.IOException;

public class JsonVaultSerializer implements IVaultSerializer {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, false);

    @Override
    public byte[] serialize(Vault vault) {
        try {
            return mapper.writeValueAsBytes(vault);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize vault", e);
        }
    }

    @Override
    public Vault deserialize(byte[] data) {
        try {
            if (data == null || data.length == 0) return new Vault();
            return mapper.readValue(data, Vault.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize vault", e);
        }
    }
}
