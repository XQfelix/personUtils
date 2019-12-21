package com.common.util.httpclient;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title Delete方法默认不支持SetEntity, 自行实现
 * @date 2019/12/10 16:42
 */
public class HttpDeleteCustom extends HttpEntityEnclosingRequestBase {
	public static final String METHOD_NAME = "DELETE";
	 
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
 
    public HttpDeleteCustom(final String uri) {
        super();
        setURI(URI.create(uri));
    }
 
    public HttpDeleteCustom(final URI uri) {
        super();
        setURI(uri);
    }
 
    public HttpDeleteCustom() {
        super();
    }

}
