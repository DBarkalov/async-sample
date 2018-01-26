package ru.sbrf.wallet.auth;

import ru.sbrf.wallet.command.Command;
import ru.sbrf.wallet.command.CommandStatus;

public class SimpleAuthActionListener<R> implements AuthActionListener<CommandStatus<R>> {
    @Override
    public void onAuthCancel() {

    }

    @Override
    public void onAuthRequired() {

    }

    @Override
    public void onCommandComplete(Command<CommandStatus<R>> mCommand, CommandStatus<R> result) {

    }

    @Override
    public void onCommandCancelled(Command<CommandStatus<R>> cmd) {

    }
}
