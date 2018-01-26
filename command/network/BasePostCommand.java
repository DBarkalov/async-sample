package ru.sbrf.wallet.command.network;

import android.util.Log;

import com.mastercard.mcbp.bhapi.BaseRequest;
import com.mastercard.mcbp.bhapi.BaseResponse;
import com.mastercard.mcbp.bhapi.ResponseHandler;
import com.mastercard.mcbp.utils.http.HttpFactory;
import com.mastercard.mcbp.utils.http.HttpPostRequest;
import com.mastercard.mcbp.utils.http.HttpResponse;
import com.mastercard.mobile_api.utils.exceptions.http.HttpException;

public abstract class BasePostCommand<P extends BaseRequest.BaseParams, R extends BaseResponse> extends BaseNetworkCommand<R, HttpPostRequest> {
    private static final String TAG = "BasePostCommand";

    @Override
    protected HttpResponse getResponse() throws HttpException {
        HttpFactory factory = getHttpFactory();
        BaseRequest<HttpPostRequest, ResponseHandler<R>> request = getRequest();
        HttpPostRequest postRequest = request.buildHttpRequest();
        Log.v(TAG, "url = " + postRequest.getUrl());
        Log.v(TAG, "post body = " + postRequest.getRequestData());
        return factory.execute(postRequest);
    }
}
