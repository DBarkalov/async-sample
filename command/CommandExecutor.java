package ru.sbrf.wallet.command;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class CommandExecutor {
    private static final String TAG = "CommandExecutor";

    private Set<Task> mRunningTasks = new HashSet<>();
    private static CommandExecutor sCommandExecutor;

    public static CommandExecutor getInstance(){
         if(sCommandExecutor == null){
             sCommandExecutor = new CommandExecutor();
          }
        return sCommandExecutor;
    }

    private CommandExecutor(){}

    public void exeTask(Command<?> cmd) {
        exeTask(cmd, null);
    }

    public void exeTask(Command<?> cmd, final CommandCompletedListener listener) {
        runTask(createTask(cmd, listener));
    }

    public void cancelAll(){
        for (Task t: mRunningTasks){
            t.cancel();
        }
        mRunningTasks.clear();
    }

    private void runTask(Task task){
        task.execute();
        mRunningTasks.add(task);
    }

    private <R> Task createTask(final Command<R> cmd, final CommandCompletedListener<R> listener) {
        return new Task<R>(cmd){
            @Override
            protected void onPostExecute(R res) {
                Log.v(TAG, "onPostExecute" + cmd.getClass().getSimpleName());
                if(listener!= null) {
                    if(cmd.isCancel()){
                        listener.onCommandCancelled(cmd);
                    } else {
                        listener.onCommandComplete(cmd, res);
                    }
                }
                onTaskCompleted(this);
            }
        };
    }

    private void onTaskCompleted(Task task){
        mRunningTasks.remove(task);
    }

    private static class Task<R> extends AsyncTask<Command<R>, Void, R> {
        private final Command<R> command;

        private Task(Command<R> command) {
            this.command = command;
        }

        public void execute(){
            Log.v(TAG, "start execute " + command.getClass().getSimpleName());
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, command);
        }

        @Override
        protected R doInBackground(Command<R>... params) {
            final Command<R> cmd = params[0];
            return cmd.execute();
        }

        public void cancel(){
            Log.v(TAG, "cancel " + command.getClass().getSimpleName());
            command.cancel();
        }

    }
}
