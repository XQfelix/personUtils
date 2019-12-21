package com.common.util.httpclient;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;


/**
 * @author GQ.Yin
 * @version 1.0
 * @title Options方法默认不支持SetEntity, 自行实现
 * @date 2019/12/10 16:52
 */
public class HttpOptionCustom extends HttpEntityEnclosingRequestBase {
	public static final String METHOD_NAME = "OPTIONS";

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

    public HttpOptionCustom() {
        super();
    }

    public HttpOptionCustom(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public HttpOptionCustom(final URI uri) {
        super();
        setURI(uri);
    }
}
