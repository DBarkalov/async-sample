package ru.sbrf.wallet.command;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class CommandGroup extends CancelledCommand {

    private final String TAG = "CommandGroup";

    private final LinkedList<CancelledCommand<?>> mCommands;
    private volatile CancelledCommand<?> mCurrentCommand;
    private CommandStatus<?> mResult;
    protected static SingleCommandExecutor sSingleCommandExecutor;

    static {
        setSingleCommandExecutor(new DefaultSingleCommandExecutor());
    }

    public CommandGroup(CancelledCommand<?>... command) {
        mCommands = new LinkedList<>();
        mCommands.addAll(Arrays.asList(command));
    }

    protected static void setSingleCommandExecutor(SingleCommandExecutor exexutor){
        sSingleCommandExecutor = exexutor;
    }

    public void addCommand(CancelledCommand<?> command){
        Log.v(TAG , "addCommand " + command.getClass().getSimpleName());
        mCommands.add(command);
    }

    @Override
    public CommandStatus execute() {
        while (!mCommands.isEmpty() && !isCancel()) {
             executeNextCommand();
        }
        return getAccumulatedResult();
    }

    public CommandStatus<?> getAccumulatedResult() {
        return mResult;
    }

    @Override
    public void cancel() {
        super.cancel();
        cancelCurrentCommand();
    }

    private void cancelCurrentCommand(){
        if(mCurrentCommand != null) {
            mCurrentCommand.cancel();
        }
    }

    protected void executeNextCommand() {
        mCurrentCommand = mCommands.pollFirst();
        final CommandStatus<?> result = executeSingleCommand();
        Log.v(TAG, "executeNextCommand command=" + mCurrentCommand.getClass().getSimpleName() + "result= " + result.toString() );
        setResult(result);
        defaultCommandStatusHandler(result);
        onCommandExecuted(mCurrentCommand, result);
    }

    protected CommandStatus<?> executeSingleCommand() {
        return sSingleCommandExecutor.execute(mCurrentCommand);
    }

    protected void defaultCommandStatusHandler(CommandStatus<?> result) {
        if (result.isError()) {
            removeAllCommands();
        }
    }

    protected void setResult(CommandStatus<?> result) {
        mResult = result;
    }

    protected void removeAllCommands(){
        mCommands.clear();
    }

    /**
     * check command result
     */
    abstract protected void onCommandExecuted(Command<?> command, CommandStatus<?> result);

    public interface SingleCommandExecutor {
        CommandStatus<?> execute(CancelledCommand<?> command);
    }

    public static  class DefaultSingleCommandExecutor implements SingleCommandExecutor {
        @Override
        public CommandStatus<?> execute(CancelledCommand<?> command) {
            return command.execute();
        }
    }

}
