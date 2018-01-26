package ru.sbrf.wallet.auth;

import ru.sbrf.wallet.command.CommandCompletedListener;

public interface AuthActionListener<R> extends CommandCompletedListener<R> {
    void onAuthCancel();
    void onAuthRequired();
}
