package ru.sbrf.wallet.command;

public interface CommandCompletedListener<R> {
    void onCommandComplete(Command<R> mCommand, R result);
    void onCommandCancelled(Command<R> cmd);
}
