package ru.sbrf.wallet.command;

public abstract class CancelledCommand<T> implements Command<CommandStatus<T>> {

    private volatile boolean  mCancel = false;

    @Override
    public void cancel() {
        mCancel = true;
    }

    public boolean isCancel() {
        return mCancel;
    }
}
