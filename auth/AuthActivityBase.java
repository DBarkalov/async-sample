package ru.sbrf.wallet.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.sbrf.wallet.BuildConfig;
import ru.sbrf.wallet.LoginActivity;
import ru.sbrf.wallet.R;
import ru.sbrf.wallet.addcard.SetOnLinePINActivity;
import ru.sbrf.wallet.bhtask.BhTaskListener;
import ru.sbrf.wallet.bhtask.BhTaskObserver;
import ru.sbrf.wallet.command.Command;
import ru.sbrf.wallet.command.CommandStatus;
import ru.sbrf.wallet.command.network.request.SendTaskResultParams;
import ru.sbrf.wallet.command.task.ResendTaskWrapper;
import ru.sbrf.wallet.command.task.Task;
import ru.sbrf.wallet.command.task.TaskSession;
import ru.sbrf.wallet.command.task.TaskType;
import ru.sbrf.wallet.data.DataCache;
import ru.sbrf.wallet.data.ResourceObserver;
import ru.sbrf.wallet.password.SendWalletPinActivity;
import ru.sbrf.wallet.settings.ChangeOnlinePinActivity;
import ru.sbrf.wallet.widgets.LockToolbar;
import ru.sbrf.wallet.widgets.ProgressDialogSpinner;

public abstract class AuthActivityBase extends AppCompatActivity implements AuthDelegate {

    public static final int AUTH_REQUEST_CODE = 154;
    public static final int SET_ONLINE_PIN_REQUEST_CODE = 155;

    private List<AuthAction> mAuthActions = new ArrayList<>();

    protected ProgressDialogSpinner mProgressDialogSpinner;

    private ResourceObserver mSessionObserver = new ResourceObserver(DataCache.SESSION_DATA_TYPE) {
        @Override
        public void onChanged() {
            onSessionChanged();
        }
    };

    protected void onSessionChanged() {
        updateLockToolbar();
    }

    protected void checkRnsTokenFlagChanged() {
        if (DataCache.getInstance().isNeedToResendRnsToken()) {
            runAction(new RnsTokenUpdateAction(this));
        }
    }

    private void updateLockToolbar() {
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null && toolbar instanceof LockToolbar) {
            LockToolbar lockToolbar = (LockToolbar) toolbar;
            if (new AccessChecker().sessionOK()) {
                lockToolbar.setLockOpen();
            } else {
                lockToolbar.setLockClose();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialogSpinner = new ProgressDialogSpinner(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BhTaskObserver.getInstance().registerListener(TaskType.SEND_CURR_WALLET_PIN, mSendCurWalletPinListener);
        BhTaskObserver.getInstance().registerListener(TaskType.SEND_CURR_ONLINE_PIN, mSetNewOnlinePinListener);
        BhTaskObserver.getInstance().registerListener(TaskType.SET_NEW_ONLINE_PIN, mSetNewOnlinePinListener);
        BhTaskObserver.getInstance().registerListener(TaskType.AUTH_REQUIRED, mResendTaskListener);

        DataCache.getInstance().registerObserver(mSessionObserver);
        updateLockToolbar();
        checkRnsTokenFlagChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BhTaskObserver.getInstance().unRegisterListener(TaskType.SEND_CURR_WALLET_PIN);
        BhTaskObserver.getInstance().unRegisterListener(TaskType.SEND_CURR_ONLINE_PIN);
        BhTaskObserver.getInstance().unRegisterListener(TaskType.SET_NEW_ONLINE_PIN);
        BhTaskObserver.getInstance().unRegisterListener(TaskType.AUTH_REQUIRED);
        DataCache.getInstance().unregisterObserver(mSessionObserver);
    }

    private final BhTaskListener mSendCurWalletPinListener = new BhTaskListener() {
        @Override
        public void onUITaskStarted(Task task) {
            startActivity(SendWalletPinActivity.getStartIntent(AuthActivityBase.this, task));
        }
    };

    private final BhTaskListener mSetNewOnlinePinListener = new BhTaskListener() {
        @Override
        public void onUITaskStarted(Task task) {
            Intent intent = null;
            switch (task.getTask()) {
                case SEND_CURR_ONLINE_PIN:
                    intent = new Intent(AuthActivityBase.this, ChangeOnlinePinActivity.class);
                    break;
                case SET_NEW_ONLINE_PIN:
                    intent = new Intent(AuthActivityBase.this, SetOnLinePINActivity.class);
                    break;
            }
            if (intent != null) {
                intent.putExtra(SetOnLinePINActivity.TASK_PARCELABLE, task);
                startActivityForResult(intent, SET_ONLINE_PIN_REQUEST_CODE);
            }
        }
    };

    private final BhTaskListener mResendTaskListener = new BhTaskListener() {
        @Override
        public void onUITaskStarted(final Task task) {
            if (task.getTask() == TaskType.AUTH_REQUIRED) {
                runAction(new ResendTaskResultAction((ResendTaskWrapper) task));
            }
        }
    };

    @Override
    public void runAction(AuthAction action) {
        try {
            mAuthActions.add(action);
            action.execute();
            mAuthActions.remove(action);
        } catch (AuthException e) {
            onAccessDenied();
        }
    }

    public void showProgress() {
        mProgressDialogSpinner.startProgress();
    }

    public void stopProgress() {
        mProgressDialogSpinner.stopProgress();
    }

    protected void onAccessDenied() {
        startActivityForResult(new Intent(this, LoginActivity.class), AUTH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onAuthOK();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                onAuthCancel();
            }
        }
    }

    private void onAuthOK() {
        try {
            Iterator<AuthAction> iter = mAuthActions.iterator();
            while (iter.hasNext()) {
                AuthAction action = iter.next();
                action.execute();
                iter.remove();
            }
        } catch (AuthException e) {
            onAccessDenied();
        }
    }

    private void onAuthCancel() {
        Iterator<AuthAction> iter = mAuthActions.iterator();
        while (iter.hasNext()) {
            AuthAction action = iter.next();
            action.cancel();
            iter.remove();
        }
    }

    public void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment);
        ft.commit();
    }

    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false);
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        if(addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    public void replaceFragmentAllowingStateLoss(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commitAllowingStateLoss();
    }

    public static class RnsTokenUpdateAction extends TargetAuthAction<AuthActivityBase> {

        protected RnsTokenUpdateAction(AuthActivityBase authActivityBase) {
            super(authActivityBase);
            setActionListener(new SimpleAuthActionListener<Object>() {
                @Override
                public void onCommandComplete(Command<CommandStatus<Object>> mCommand, CommandStatus<Object> result) {
                    if (getTarget() != null) {
                        if (BuildConfig.isDemo) {
                            String updateRnsResult = result.isOK() ? "Rns-token was updated" : "Couldn't update rns-token!";
                            Toast.makeText(getTarget(), updateRnsResult, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public void execute() throws AuthException {
            String rnsToken = FirebaseInstanceId.getInstance().getToken();
            RequestManager.getInstance().updateRnsTokenRemote(rnsToken, getCommandListener());
        }
    }

    public static class ResendTaskResultAction implements AuthAction {

        private final ResendTaskWrapper mTaskWrapper;

        public ResendTaskResultAction(ResendTaskWrapper task) {
            mTaskWrapper = task;
        }

        @Override
        public void execute() throws AuthException {
            new AccessChecker().check();
            TaskSession newTaskSession = new TaskSession(
                    mTaskWrapper.getTaskSession().getHceSessionId(),
                    DataCache.getInstance().getSession().getSessionId(),
                    mTaskWrapper.getTaskSession().getWallet());
            Task rewindTask = mTaskWrapper.copyWithNewSession(newTaskSession);
            SendTaskResultParams rewindTaskResult = mTaskWrapper
                    .getSavedTaskResultParams().copyWithNewSession(newTaskSession.getBhSessionId());
            RequestManager.getInstance().completeHCETask(rewindTask, rewindTaskResult,
                    mTaskWrapper.getTaskResultListener());
        }

        @Override
        public void cancel() {
            //TODO do something
        }
    }
}
