package service;

import javax.crypto.SecretKey;

public interface IAuthService {
    boolean isFirstRun();

    void setupMasterPassword(char[] password);

    SecretKey login(char[] password);
}
