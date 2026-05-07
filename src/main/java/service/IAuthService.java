package service;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface IAuthService {
    boolean isFirstRun();

    SecretKey setupMasterPassword(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException;

    SecretKey login(char[] password, byte[] existingSalt) throws NoSuchAlgorithmException, InvalidKeySpecException;

    byte[] getSalt();
}
