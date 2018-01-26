package ru.sbrf.wallet.command.network;

import com.mastercard.mcbp.bhapi.BaseRequest;
import com.mastercard.mcbp.bhapi.BaseResponse;
import com.mastercard.mcbp.bhapi.ResponseHandler;
import com.mastercard.mcbp.utils.http.HttpFactory;
import com.mastercard.mcbp.utils.http.HttpGetRequest;
import com.mastercard.mcbp.utils.http.HttpResponse;
import com.mastercard.mobile_api.utils.exceptions.http.HttpException;

public abstract class BaseGetCommand<P extends BaseRequest.BaseParams, R extends BaseResponse> extends BaseNetworkCommand<R, HttpGetRequest> {
    @Override
    protected HttpResponse getResponse() throws HttpException {
        HttpFactory factory = getHttpFactory();
        BaseRequest<HttpGetRequest, ResponseHandler<R>> request = getRequest();
        return factory.execute(request.buildHttpRequest());
    }
}
