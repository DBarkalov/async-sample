package ru.sbrf.wallet.command;

/**
 * Created by dima on 24.12.15.
 *  R - result type
 */
public interface Command<R> {
    R execute();
    void cancel();
    boolean isCancel();
}
