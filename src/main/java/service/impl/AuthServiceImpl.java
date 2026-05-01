package service.impl;

import service.IAuthService;

import javax.crypto.SecretKey;

public class AuthServiceImpl implements IAuthService {
    @Override
    public boolean isFirstRun() {
        return false;
    }

    @Override
    public void setupMasterPassword(char[] password) {

    }

    @Override
    public SecretKey login(char[] password) {
        return null;
    }
}
