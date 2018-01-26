package ru.sbrf.wallet.command.network.request;

import com.mastercard.mcbp.bhapi.BaseRequest;
import com.mastercard.mcbp.utils.http.AndroidHttpPostRequest;
import com.mastercard.mcbp.utils.http.HttpPostRequest;

public abstract class BasePostRequest<H> extends BaseRequest<HttpPostRequest, H> {

    protected BasePostRequest(String baseUrl, BaseParams requestParams) {
        super(baseUrl, requestParams);
    }

    @Override
    public HttpPostRequest buildHttpRequest() {
        return new AndroidHttpPostRequest().withUrl(buildUrl()).withRequestData(getParams().toJson());
    }

}
