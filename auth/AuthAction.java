package ru.sbrf.wallet.auth;

public interface AuthAction {
    void execute() throws AuthException;

    void cancel();
}
