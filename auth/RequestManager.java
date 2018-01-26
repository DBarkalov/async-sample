package ru.sbrf.wallet.auth;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mastercard.mcbp.init.McbpInitializer;
import com.mastercard.mobile_api.bytes.ByteArray;

import java.util.List;

import ru.sbrf.wallet.BuildConfig;
import ru.sbrf.wallet.bhtask.BhTaskObserver;
import ru.sbrf.wallet.command.BindCardCommandGroup;
import ru.sbrf.wallet.command.CardActivationGroup;
import ru.sbrf.wallet.command.Command;
import ru.sbrf.wallet.command.CommandCompletedListener;
import ru.sbrf.wallet.command.CommandExecutor;
import ru.sbrf.wallet.command.CommandStatus;
import ru.sbrf.wallet.command.LoginCommandGroup;
import ru.sbrf.wallet.command.OpenHCESessionGroup;
import ru.sbrf.wallet.command.ResendTaskResultGroup;
import ru.sbrf.wallet.command.VerifyLoginCommandGroup;
import ru.sbrf.wallet.command.WipeCardsGroup;
import ru.sbrf.wallet.command.network.ChangeCardStatusCommand;
import ru.sbrf.wallet.command.network.GetNewSmsOtpCommand;
import ru.sbrf.wallet.command.network.InitiatePinSettingCommand;
import ru.sbrf.wallet.command.network.RegisterWalletGroup;
import ru.sbrf.wallet.command.network.SmsAuthCommand;
import ru.sbrf.wallet.command.network.UpdateRnsTokenCommand;
import ru.sbrf.wallet.command.network.request.CardStatus;
import ru.sbrf.wallet.command.network.request.ChangeCardStatusParams;
import ru.sbrf.wallet.command.network.request.GetNewSmsOtpParams;
import ru.sbrf.wallet.command.network.request.InitiatePinSettingParams;
import ru.sbrf.wallet.command.network.request.PinOperation;
import ru.sbrf.wallet.command.network.request.SendTaskResultParams;
import ru.sbrf.wallet.command.network.request.SmsAuthParams;
import ru.sbrf.wallet.command.network.request.UpdateRnsTokenParams;
import ru.sbrf.wallet.command.network.request.UpdateRnsTokenResponse;
import ru.sbrf.wallet.command.task.ResendTaskWrapper;
import ru.sbrf.wallet.command.task.Task;
import ru.sbrf.wallet.data.DataCache;
import ru.sbrf.wallet.data.DataManager;
import ru.sbrf.wallet.models.LocalCard;
import ru.sbrf.wallet.models.Session;

public class RequestManager {

    private static final String TAG = "RequestManager";
    private static RequestManager sRequestManager;
    private final CommandExecutor mCommandExecutor = CommandExecutor.getInstance();
    private DataCache mDataCache = DataCache.getInstance();
    private DataManager mDataManager = DataManager.INSTANCE;

    private CommandCompletedListener mTaskCompletedListener = new CommandCompletedListener() {
        @Override
        public void onCommandComplete(Command mCommand, Object result) {

        }

        @Override
        public void onCommandCancelled(Command cmd) {

        }
    };

    public static RequestManager getInstance() {
        if (sRequestManager == null) {
            sRequestManager = new RequestManager();
        }
        return sRequestManager;
    }

    private RequestManager() {
    }

    public void setTaskCompletedListener(CommandCompletedListener taskCompletedListener) {
        mTaskCompletedListener = taskCompletedListener;
    }

    public void requestVerifyLogin(String login, CommandCompletedListener listener) {
        mCommandExecutor.exeTask(new VerifyLoginCommandGroup(login), listener);
    }

    public void requestLogin(String password, CommandCompletedListener listener) {
        mCommandExecutor.exeTask(new LoginCommandGroup(password, DataManager.INSTANCE.getGuid()), listener);
    }

    public void resendGetNewSmsOtp(CommandCompletedListener listener) throws AuthException {
        checkAccess();
        final GetNewSmsOtpParams params = new GetNewSmsOtpParams(DataCache.getInstance().getSession().getSessionId());
        mCommandExecutor.exeTask(new GetNewSmsOtpCommand(params), listener);
    }

    public void requestSmsAuth(final Context context, final String smsCode, final CommandCompletedListener listener) throws AuthException {
        checkAccess();
        Session currSession = DataCache.getInstance().getSession();
        SmsAuthParams params = new SmsAuthParams(currSession.getSessionId(), smsCode);
        mCommandExecutor.exeTask(new SmsAuthCommand(params), listener);
    }

    public void registerWallet(final Context appContext, final String smsCode, CommandCompletedListener listener) throws AuthException {
        //new AccessChecker().check();
        Session currSession = DataCache.getInstance().getSession();
        SmsAuthParams smsParams = new SmsAuthParams(currSession.getSessionId(), smsCode);
        mCommandExecutor.exeTask(new RegisterWalletGroup(appContext, smsParams), listener);
    }

    public void requestBindCard(final LocalCard localCard, CommandCompletedListener listener) throws AuthException {
        checkAccess();
        final String sessionId = DataCache.getInstance().getSession().getSessionId();
        // TODO check LDE and EnvContainer?
        final String walletId = McbpInitializer.getInstance().getLdeRemoteManagementService().getEnvContainer().getCmsMpaId().toHexString();
        mCommandExecutor.exeTask(new BindCardCommandGroup(sessionId, walletId, localCard), listener);
    }

    public void requestInitiatePinSetting(PinOperation operation, CommandCompletedListener listener) throws AuthException {
        checkAccess();
        final String sessionId = DataCache.getInstance().getSession().getSessionId();
        // TODO check LDE and EnvContainer?
        final String walletId = McbpInitializer.getInstance().getLdeRemoteManagementService().getEnvContainer().getCmsMpaId().toHexString();
        final InitiatePinSettingParams params = new InitiatePinSettingParams(operation, sessionId, walletId);
        mCommandExecutor.exeTask(new InitiatePinSettingCommand(params), listener);
    }

    public void requestInitiateCardPinChanging(String cardId, PinOperation opType, CommandCompletedListener listener) throws AuthException {
        checkAccess();
        final String sessionId = DataCache.getInstance().getSession().getSessionId();
        // TODO check LDE and EnvContainer?
        final String walletId = McbpInitializer.getInstance().getLdeRemoteManagementService().getEnvContainer().getCmsMpaId().toHexString();
        final InitiatePinSettingParams params = new InitiatePinSettingParams(opType, sessionId, walletId, cardId);

        switch (opType) {
            case CHANGE:
                mCommandExecutor.exeTask(new InitiatePinSettingCommand(params), listener);
                break;
            case SET:
                mCommandExecutor.exeTask(new CardActivationGroup(cardId,
                        new InitiatePinSettingCommand(params)), listener);
                break;
        }
    }

    public void requestChangeCardStatus(String cardNumber, String endDate, CardStatus cardStatus, CommandCompletedListener listener) throws AuthException {
        checkAccess();
        final String sessionId = DataCache.getInstance().getSession().getSessionId();
        // TODO check LDE and EnvContainer?
        final String walletId = McbpInitializer.getInstance().getLdeRemoteManagementService().getEnvContainer().getCmsMpaId().toHexString();
        final ChangeCardStatusParams.CardInfo cardInfo = new ChangeCardStatusParams.CardInfo(cardNumber, endDate, cardStatus);
        final ChangeCardStatusParams params = new ChangeCardStatusParams(sessionId, walletId, cardInfo);
        mCommandExecutor.exeTask(new ChangeCardStatusCommand(params), listener);
    }

    public void deleteCards(List<String> cardsIds, CommandCompletedListener listener) throws AuthException {
        checkAccess();
        mCommandExecutor.exeTask(new WipeCardsGroup(cardsIds), listener);
    }

    public void openHCESession(String bhSessionId, ByteArray rnsMsg, CommandCompletedListener listener) {
        Log.d(TAG, "ANDROID_RNS;RECEIVED_DATA (HEX):([" + rnsMsg.toHexString() + "])");
        mCommandExecutor.exeTask(new OpenHCESessionGroup(bhSessionId, rnsMsg), new OpenHCESessionListener(listener));
    }

    public void updateRnsTokenRemote(String rnsToken, @Nullable CommandCompletedListener listener) throws AuthException {
        checkAccess();
        Session currSession = mDataCache.getSession();
        String mGuid = mDataManager.getGuid();
        if (mGuid != null) {
            UpdateRnsTokenParams reqParams = new UpdateRnsTokenParams(
                    currSession.getSessionId(), mGuid, rnsToken,
                    BuildConfig.APPLICATION_ID, //TODO FIXME
                    BuildConfig.VERSION_NAME
            );
            //turn off flag for execution process
            if (mDataCache.isNeedToResendRnsToken()) {
                mDataCache.setNeedToResendRnsToken(false);
            }
            mCommandExecutor.exeTask(new UpdateRnsTokenCommand(reqParams),
                    new UpdateRnsTokenListener(listener));
        } else {
            Log.w(TAG, "It's to early to change rns token - we don't have registration yet");
        }
    }

    public class UpdateRnsTokenListener implements
            CommandCompletedListener<CommandStatus<UpdateRnsTokenResponse>> {

        private CommandCompletedListener mExtListener;

        public UpdateRnsTokenListener(@Nullable CommandCompletedListener extListener) {
            mExtListener = extListener;
        }

        @Override
        public void onCommandComplete(Command<CommandStatus<UpdateRnsTokenResponse>> command,
                                      CommandStatus<UpdateRnsTokenResponse> result) {
            if (!result.isOK()) {
                mDataCache.setNeedToResendRnsToken(true);
            }
            if (mExtListener != null) {
                mExtListener.onCommandComplete(command, result);
            }
        }

        @Override
        public void onCommandCancelled(Command cmd) {
            mDataCache.setNeedToResendRnsToken(true);
            if (mExtListener != null) {
                mExtListener.onCommandCancelled(cmd);
            }
        }
    }

    public class OpenHCESessionListener implements CommandCompletedListener {
        private final CommandCompletedListener completedListener;

        public OpenHCESessionListener(CommandCompletedListener completedListener) {
            this.completedListener = completedListener;
        }

        @Override
        public void onCommandComplete(Command command, Object result) {
            if (completedListener != null) {
                completedListener.onCommandComplete(command, result);
            }
            if (command instanceof OpenHCESessionGroup) {
                if (result instanceof CommandStatus.OK && ((CommandStatus.OK) result).getData() instanceof Task) {
                    Log.d(TAG, "session open ok");
                    Task r = (Task) ((CommandStatus.OK) result).getData();
                    executeHCETask(r);
                } else {
                    Log.d(TAG, "session open error");
                }
            }
        }

        @Override
        public void onCommandCancelled(Command cmd) {
        }
    }

    public void executeHCETask(Task<?> task) {
        if (task.isUiTask()) {
            BhTaskObserver.getInstance().onTask(task);
        } else {
            continueHCETask(task, null);
        }
    }

    public void continueHCETask(Task<?> task, CommandCompletedListener listener) {
        mCommandExecutor.exeTask(task.getCommand(), new ExecuteHCETaskListener(task, listener));
    }

    public void completeHCETask(Task<?> task, SendTaskResultParams resultParams,
                                CommandCompletedListener listener) {
        mCommandExecutor.exeTask(new ResendTaskResultGroup(task, resultParams),
                new ExecuteHCETaskListener(task, listener));
    }

    private class ExecuteHCETaskListener implements CommandCompletedListener<CommandStatus> {
        private final CommandCompletedListener mCompletedListener;
        private final Task mInitialTask;


        public ExecuteHCETaskListener(Task task,
                                      @Nullable CommandCompletedListener listener) {
            this.mCompletedListener = listener;
            mInitialTask = task;
        }

        @Override
        public void onCommandComplete(Command mCommand, CommandStatus result) {
            getInstance().mTaskCompletedListener.onCommandComplete(mCommand, result);
            if (result instanceof CommandStatus.SESSION_FAIL && result.getData() instanceof SendTaskResultParams) {
                DataCache.getInstance().setSession(null);
                SendTaskResultParams savedTaskResultParams = (SendTaskResultParams) result.getData();
                executeHCETask(new ResendTaskWrapper(mInitialTask, savedTaskResultParams, mCompletedListener));
            } else {
                if (mCompletedListener != null) {
                    mCompletedListener.onCommandComplete(mCommand, result);
                }
                // TODO надо протестить когда тасок нет больше
                if (result.isOK() && result.getData() instanceof Task && !(result instanceof CommandStatus.END_OF_TASKS)) {
                    final Task task = ((CommandStatus.OK<Task>) result).getData();
                    executeHCETask(task);
                }
            }
        }

        @Override
        public void onCommandCancelled(Command cmd) {
            getInstance().mTaskCompletedListener.onCommandCancelled(cmd);
        }

    }

    private void checkAccess() throws AuthException {
        new AccessChecker().check();
    }
}
