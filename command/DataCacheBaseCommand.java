package ru.sbrf.wallet.command;

import android.os.Handler;
import android.os.Looper;

import ru.sbrf.wallet.data.DataCache;

public abstract class DataCacheBaseCommand extends CancelledCommand<Void> {
    private final DataCache mDataCache;
    private final Handler mHandler;
    private Runnable mRunnable;

    public DataCacheBaseCommand(final DataCache dataCache) {
        mDataCache = dataCache;
        mRunnable = getRunnable();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public CommandStatus<Void> execute() {
        mHandler.post(mRunnable);
        return new CommandStatus.OK<>();
    }

    protected Runnable getRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateDataCache(mDataCache);
                mRunnable = null;
            }
        };
    }

    protected abstract void updateDataCache(DataCache cache);

    @Override
    public void cancel() {
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}