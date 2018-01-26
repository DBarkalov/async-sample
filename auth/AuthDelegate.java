package ru.sbrf.wallet.auth;

public interface AuthDelegate {
    void runAction(AuthAction action);
}
