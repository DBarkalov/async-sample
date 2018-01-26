package ru.sbrf.wallet.command.network.request;

import com.mastercard.mcbp.bhapi.BaseRequest;
import com.mastercard.mcbp.utils.http.AndroidHttpGetRequest;
import com.mastercard.mcbp.utils.http.HttpGetRequest;

public abstract class BaseGetRequest<H> extends BaseRequest<HttpGetRequest, H> {

    protected BaseGetRequest(String baseUrl, BaseParams requestParams) {
        super(baseUrl, requestParams);
    }

    @Override
    public HttpGetRequest buildHttpRequest() {
        return new AndroidHttpGetRequest().withUrl(buildUrl());
    }

}
