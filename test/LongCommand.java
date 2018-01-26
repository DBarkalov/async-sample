package ru.sbrf.wallet.command;

import android.util.Log;

public class LongCommand extends CancelledCommand<Integer> {

    private static final String TAG = "LongCommand";
    private int k = 3;
    private boolean executed;

    public LongCommand(int m) {
        this.k = k * m;
    }

    @Override
    public CommandStatus<Integer> execute() {
        executed = true;
        Log.v(TAG, "execute start");
        for (int i = 0; i < 10; i++) {
            if (isCancel()) {
                Log.v(TAG, "execute cancel");
                return new CommandStatus.CANCEL();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "execute end ");
        return new CommandStatus.OK<>(k);
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
