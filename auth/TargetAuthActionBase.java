package ru.sbrf.wallet.auth;

import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import ru.sbrf.wallet.command.Command;
import ru.sbrf.wallet.command.CommandCompletedListener;
import ru.sbrf.wallet.command.CommandStatus;
import ru.sbrf.wallet.data.DataCache;

public abstract class TargetAuthActionBase<Target extends AuthDelegate, Result> implements AuthAction {
    private final WeakReference<Target> mTarget;
    private final AuthActionListener<Result> mBaseListener;
    private AuthActionListener<Result> mExternalListener;

    protected TargetAuthActionBase(Target t) {
        mTarget = new WeakReference<>(t);
        mBaseListener = new BaseAuthActionListener();
    }

    public Target getTarget() {
        return mTarget.get();
    }

    @Override
    public final void cancel() {
        mBaseListener.onAuthCancel();
    }

    protected final void setActionListener(@Nullable AuthActionListener listener) {
        mExternalListener = listener;
    }

    protected final CommandCompletedListener<Result> getCommandListener() {
        return mBaseListener;
    }


    private final class BaseAuthActionListener implements AuthActionListener<Result> {
        @Override
        public void onAuthCancel() {
            if (mExternalListener != null) {
                mExternalListener.onAuthCancel();
            }
        }

        @Override
        public void onAuthRequired() {
            if (getTarget() != null) {
                //rerun action
                getTarget().runAction(TargetAuthActionBase.this);
            }
            if (mExternalListener != null) {
                mExternalListener.onAuthRequired();
            }
        }

        @Override
        public void onCommandComplete(Command<Result> mCommand, Result result) {
            if (getTarget() != null) {
                if (result instanceof CommandStatus.SESSION_FAIL) {
                    DataCache.getInstance().setSession(null);
                    onAuthRequired();
                } else if (mExternalListener != null) {
                    mExternalListener.onCommandComplete(mCommand, result);
                }
            }
        }

        @Override
        public void onCommandCancelled(Command<Result> cmd) {
            if (mExternalListener != null) {
                mExternalListener.onCommandCancelled(cmd);
            }
        }
    }
}
