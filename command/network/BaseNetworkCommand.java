package ru.sbrf.wallet.command.network;

import android.util.Log;

import com.mastercard.mcbp.bhapi.BaseRequest;
import com.mastercard.mcbp.bhapi.BaseResponse;
import com.mastercard.mcbp.bhapi.ResponseHandler;
import com.mastercard.mcbp.utils.http.HttpFactory;
import com.mastercard.mcbp.utils.http.HttpResponse;
import com.mastercard.mobile_api.utils.exceptions.http.HttpException;

import ru.sbrf.wallet.BuildConfig;
import ru.sbrf.wallet.command.CancelledCommand;
import ru.sbrf.wallet.command.CommandStatus;

public abstract class BaseNetworkCommand<R extends BaseResponse, P> extends CancelledCommand<R> {

    private static final String TAG = "BaseNetworkCommand";

    private static HttpFactory sHttpFactory;

    private BaseRequest<P, ResponseHandler<R>> mRequest;

    public static void setHttpFactory(HttpFactory factory) {
        sHttpFactory = factory;
    }

    protected HttpFactory getHttpFactory() {
        return sHttpFactory;
    }

    protected String getCmsBaseUrl() {
        return BuildConfig.CMS_URL;
    }

    protected abstract BaseRequest<P, ResponseHandler<R>> buildHttpRequest();

    protected BaseRequest<P, ResponseHandler<R>> getRequest() {
        return mRequest;
    }

    @Override
    public CommandStatus<R> execute() {
        mRequest = buildHttpRequest();

        try {
            HttpResponse response = getResponse();
            Log.v(TAG, "response status code =  " + response.getStatusCode());
            Log.v(TAG, "response = " + response.getContent().toUtf8String().replace("\n", "").replace("\r", ""));

            if (response.getStatusCode() != 200) {
                return new CommandStatus.ERROR("server error status code = " + response.getStatusCode());
            }

            R result = parseResponse(response);
            Log.v(TAG, "result.getResultCode() = " + result.getResultCode());

            if (!result.statusOK()) {
                if (result.getResultCode() == 1006) {
                    return new CommandStatus.SESSION_FAIL<>(result);
                } else {
                    return new CommandStatus.BH_ERROR<>(result);
                }
            }

            return new CommandStatus.OK<>(result);
        } catch (HttpException e) {
            return new CommandStatus.NETWORK_ERROR(e);
        } catch (ClassCastException e) {
            return new CommandStatus.PARSING_RESPONSE_ERROR(e);
        }
    }

    private R parseResponse(HttpResponse response) throws ClassCastException {
        BaseRequest<?, ResponseHandler<R>> request = getRequest();
        ResponseHandler<R> handler = request.getResponseHandler();
        return handler.parseResponse(response.getContent().toUtf8String());
    }

    protected abstract HttpResponse getResponse() throws HttpException;
}
