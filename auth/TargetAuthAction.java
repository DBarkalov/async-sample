package ru.sbrf.wallet.auth;

import ru.sbrf.wallet.command.CommandStatus;

public abstract class TargetAuthAction<T extends AuthDelegate> extends TargetAuthActionBase<T, CommandStatus<Object>> {
    protected TargetAuthAction(T t) {
        super(t);
    }
}
