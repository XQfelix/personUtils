package com.common.util.httpclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * @title HTTP请求客户端(支持HTTPS), 实现基于Apache HttpClient4.x
 * 		   支持请求包含:
 * 				GET 请求指定的页面信息，并返回实体主体
 * 				HEAD 请求获取报头信息
 * 				OPTIONS 请求查看服务端性能, 以及支持的请求方式等等信息
 * 				DELETE 请求服务器删除指定页面
 * 				TRACE 回显服务器收到的请求，主要用于测试或诊断, 该方式需要服务端开启该方式(大多数服务端该方式禁用)
 * 				PUT 从客户端向服务器传送的数据取代指定的文档的内容
 * 				PATCH 从客户端向服务器传送的数据取代指定的文档的内容,局部更新
 * 				POST 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）
 *         支持请求Content-Type类型包含:
 *              multipart/form-data
 *				application/x-www-form-urlencoded
 *				application/json
 *				application/xml
 *				text/plain
 *				text
 *
 * @author GQ.Yin
 * @version 1.0
 * @date 2019/12/10 17:42
 */
@SuppressWarnings("deprecation")
public class HttpClient extends DefaultHttpClient {
	private static Logger logger = Logger.getLogger(HttpClient.class);

	
	/**
	 * 向特定的资源发出Get请求(支持HTTPS)
	 * 
	 *@param url
	 *@param header
	 *@param charset
	 * */
	public static String doGet(String url, Map<String, Object> header, String charset) {
		HttpGet httpGet = new HttpGet(url);
		return baseEmptyRequest(httpGet, url, header, charset);
	}
	
	
	/**
	 * 请求服务器删除Request-URI所标识的资源(支持HTTPS)
	 * 
	 *@param url
	 *@param header
	 *@param charset
	 * */
	public static String doDelete(String url, Map<String, Object> header, String charset) {
		HttpDelete httpDelete = new HttpDelete(url);
		return baseEmptyRequest(httpDelete, url, header, charset);
	}
	
	
	/**
	 * 返回服务器针对特定资源所支持的HTTP请求方法(支持HTTPS)
	 * 
	 *@param url
	 *@param header
	 *@param charset
	 * */
	public static String doOptions(String url, Map<String, Object> header, String charset) {
		HttpOptions httpOptions = new HttpOptions(url);
		return baseEmptyRequest(httpOptions, url, header, charset);
	}
	
	
	/**
	 * 向服务器索要与GET请求相一致的响应，响应体将不会被返回 (支持HTTPS)
	 * 可以获取包含在响应消息头中的元信息
	 * 
	 *@param url
	 *@param header
	 *@param charset
	 * */
	public static String doHead(String url, Map<String, Object> header, String charset) {
		HttpHead httpHead = new HttpHead(url);
		return baseEmptyRequest(httpHead, url, header, charset);
	}
	
	
	
	/**
	 * 回显服务器收到的请求，主要用于测试或诊断(支持HTTPS)
	 * 
	 *@param url
	 *@param header
	 *@param charset
	 * */
	public static String doTrace(String url, Map<String, Object> header, String charset) {
		HttpTrace httpTrace = new HttpTrace(url);
		return baseEmptyRequest(httpTrace, url, header, charset);
	}
	
	
	
	/**
	 * 向指定资源提交数据进行处理请求,例如提交表单或者上传文件(支持HTTPS)
	 * 
	 *@param url
	 *@param header 
	 *		Content-Type支持的类型有:
	 *			multipart/form-data
	 *			application/x-www-form-urlencoded
	 *			application/json
	 *			application/xml
	 *			text/plain
	 *			text
	 *
	 *@param param 
	 *		当Content-Type是text/plain，参数的数据结构是String("abcdefg")
	 *		当Content-Type为其他时
	 *
	 *@param charset
	 * */
	public static String doPost(String url, Map<String, Object> header, Object param, String charset) {
		HttpPost httpPost = new HttpPost(url);
		return baseEntityRequest(httpPost, url, header, param, new ArrayList<String>(), charset);
	}
	
	
	/**
	 **向指定资源提交数据进行处理请求,例如提交表单或者上传文件(支持HTTPS)
	 **
	 **若要发送文件附件, Content-Type必须为multipart/form-data
	 **
	 **
	 *@param url
	 *@param header 
	 *		Content-Type支持的类型有:
	 *			multipart/form-data
	 *			application/x-www-form-urlencoded
	 *			application/json
	 *			application/xml
	 *			text/plain
	 *			text
	 *
	 *@param param 
	 *		当Content-Type是text/plain，参数的数据结构是String("abcdefg")
	 *		当Content-Type为其他时
	 *
	 *@param uploadFilePaths 
	 *
	 *@param charset
	 * */
	public static String doPost(String url, Map<String, Object> header, Object param, List<String> uploadFilePaths, String charset) {
		HttpPost httpPost = new HttpPost(url);
		return baseEntityRequest(httpPost, url, header, param, uploadFilePaths, charset);
	}
	
	
	/**
	 * 向指定资源位置上传其最新内容(支持HTTPS)
	 * 
	 *@param url
	 *@param header
	 *@param param
	 *@param charset
	 * */
	public static String doPut(String url, Map<String, Object> header, Object param, String charset) {
		HttpPut httpPut = new HttpPut(url);
		return baseEntityRequest(httpPut, url, header, param,  new ArrayList<String>(), charset);
	}
	
	
	/**
	 **向指定资源位置上传其最新内容(支持HTTPS)
	 **
	 **若要发送文件附件, Content-Type必须为multipart/form-data
	 **
	 **
	 *@param url
	 *@param header 
	 *		Content-Type支持的类型有:
	 *			multipart/form-data
	 *			application/x-www-form-urlencoded
	 *			application/json
	 *			application/xml
	 *			text/plain
	 *			text
	 *
	 *@param param 
	 *		当Content-Type是text/plain，参数的数据结构是String("abcdefg")
	 *		当Content-Type为其他时, 参数为Map
	 *
	 *@param uploadFilePaths 
	 *
	 *@param charset
	 * */
	public static String doPut(String url, Map<String, Object> header, Object param, List<String> uploadFilePaths, String charset) {
		HttpPut httpPut = new HttpPut(url);
		return baseEntityRequest(httpPut, url, header, param, uploadFilePaths, charset);
	}

	/**
	 * 向指定资源位置上传内容局部更新支持HTTPS)
	 *
	 *@param url
	 *@param header
	 *@param param
	 *@param charset
	 * */
	public static String doPatch(String url, Map<String, Object> header, Object param, String charset) {
		HttpPatch httpPatch = new HttpPatch(url);
		return baseEntityRequest(httpPatch, url, header, param,  new ArrayList<String>(), charset);
	}

	/**
	 **向指定资源位置上传其最新内容(支持HTTPS)
	 **
	 **若要发送文件附件, Content-Type必须为multipart/form-data
	 **
	 **
	 *@param url
	 *@param header
	 *		Content-Type支持的类型有:
	 *			multipart/form-data
	 *			application/x-www-form-urlencoded
	 *			application/json
	 *			application/xml
	 *			text/plain
	 *			text
	 *
	 *@param param
	 *		当Content-Type是text/plain，参数的数据结构是String("abcdefg")
	 *		当Content-Type为其他时, 参数为Map
	 *
	 *@param uploadFilePaths
	 *
	 *@param charset
	 * */
	public static String doPatch(String url, Map<String, Object> header, Object param, List<String> uploadFilePaths, String charset) {
		HttpPatch httpPatch = new HttpPatch(url);
		return baseEntityRequest(httpPatch, url, header, param, uploadFilePaths, charset);
	}


	/**
	 * @title 文件下载
	 *
	 * @param url
	 * @param filepath  文件存放全路径("C:\\test.png")
	 * */
	public static String down(String url, String filepath) throws FileNotFoundException, HttpProcessException {
		String ret = "";
		File file = new File(filepath);
		HttpClientUtil.down(HttpConfig.custom().url(url).out(new FileOutputStream(file)));
		if (file.exists()) {
			ret = "true";
			logger.info("File Download Success, Sava path: " + file.getPath());
		}
		return ret;
	}
	
	
	/**
	 * 浏览器传递参数基础请求
	 */
	private static String baseEmptyRequest(HttpRequestBase requestMethod, String url, Map<String, Object> header, String charset) {
		BasicConfigurator.configure();
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		// 对HTTPS请求进行处理
        if (url.toLowerCase().startsWith("https://")) {
        	try {
				httpClient = new HttpClient("TLSv1.2");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		if (header != null && header.size() > 0) { // 设置请求头
			header.forEach((key, value) -> {
				requestMethod.addHeader(key, value.toString());
			});
		}
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(35000)
				.setConnectionRequestTimeout(35000)
				.setSocketTimeout(60000).build();
		requestMethod.setConfig(requestConfig);
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(requestMethod);
			HttpEntity entity = httpResponse.getEntity();
			Map<String, Object> headerMap = new HashMap<>();
			Header[] headers = httpResponse.getAllHeaders();
			if (headers != null && headers.length > 0) {
				for (int i =0; i< headers.length; i++) {
					Header header1 = headers[i];
					headerMap.put(header1.getName(), header1.getValue());
				}
			}
			if(requestMethod.getMethod().equals("HEAD")||requestMethod.getMethod().equals("OPTIONS")) {
				result = new JSONObject(headerMap).toJSONString();
			}else {
				JSONObject retObj = new JSONObject();
				retObj.put("ResponseHeaders", new JSONObject(headerMap).toJSONString());
				retObj.put("ResponseBody", EntityUtils.toString(entity, (charset==null || charset.equals("")) ? "UTF-8" : charset));
				result = retObj.toJSONString();
			}
			logger.debug("------Request Success!!!" + result);
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	
	
	/**
	 * Body传递参数基础请求
	 */
	@SuppressWarnings("unchecked")
	private static String baseEntityRequest(HttpEntityEnclosingRequestBase requestMethod, String url, Map<String, Object> header, Object param, List<String> uploadFilePaths, String charset){
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String result = "";
		// 对HTTPS请求进行处理
        if (url.toLowerCase().startsWith("https://")) {
        	try {
				httpClient = new HttpClient("TLSv1.2");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(120000)
				.setConnectionRequestTimeout(120000)
				.setSocketTimeout(120000).build();
		requestMethod.setConfig(requestConfig);
		String contentType = "";
		if (header != null && header.size() > 0) { // 设置请求头
			Iterator it=header.keySet().iterator();
			while(it.hasNext()){
				String key=it.next().toString();
				if (key.toLowerCase().equals("content-type")) {
					if (String.valueOf(header.get(key)).contains(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
						contentType = ContentType.MULTIPART_FORM_DATA.getMimeType();
						continue;
					}
				}
				requestMethod.addHeader(key, header.get(key).toString());
			}
		} else {
			logger.debug("Content-Type is null，Default：application/json");
			contentType = "application/json";
		}
		CloseableHttpResponse httpResponse = null;
		if (null != param || ContentType.MULTIPART_FORM_DATA.getMimeType().contains(contentType)) {
			if (charset == null || charset.equals("")) {charset = "UTF-8";}
			if(ContentType.APPLICATION_JSON.getMimeType().contains(contentType)){
				param = (Map<String, Object>)param;
				requestMethod.setEntity(new StringEntity(JSON.toJSONString(param), ContentType.create("application/json", charset)));
			}else if (ContentType.APPLICATION_FORM_URLENCODED.getMimeType().contains(contentType)) {
				Map<String, Object> paramTemp = (Map<String, Object>)param;
				List<NameValuePair> nvp = new ArrayList<NameValuePair>();
				paramTemp.forEach((key, value) -> {
					nvp.add(new BasicNameValuePair(key, value.toString()));
				});
				try {
					requestMethod.setEntity(new UrlEncodedFormEntity(nvp, charset));
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(), e);
				}
			}else if(ContentType.MULTIPART_FORM_DATA.getMimeType().contains(contentType) ||
					contentType.contains(ContentType.MULTIPART_FORM_DATA.getMimeType())){
				Map<String, ContentBody> reqParam = new HashMap<>();
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				if(null != param){
					Map<String, Object> paramTemp = (Map<String, Object>) param;
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					paramTemp.forEach((key, value) -> {
						reqParam.put(key, new StringBody(value.toString(), ContentType.MULTIPART_FORM_DATA));
					});
				}
				uploadFilePaths.forEach(key -> {
					File file = new File(key);
					builder.addPart(file.getName(), new FileBody(file));
				});
				reqParam.forEach((key, value) -> {
					builder.addPart(key, value);
				});
				builder.setCharset(Charset.forName(charset));
				requestMethod.setEntity(builder.build());
			}else if(ContentType.TEXT_PLAIN.getMimeType().contains(contentType)){
				requestMethod.setEntity(new StringEntity(param.toString(), ContentType.create("text/plain", charset)));
			}else if(ContentType.APPLICATION_XML.getMimeType().contains(contentType)){
				requestMethod.setEntity(new StringEntity(param.toString(), ContentType.APPLICATION_XML));
			}
		}
		try {
			// httpClient对象执行post请求,并返回响应参数对象
			httpResponse = httpClient.execute(requestMethod);
			Map<String, Object> headerMap = new HashMap<>();
			Header[] headers = httpResponse.getAllHeaders();
			if (headers != null && headers.length > 0) {
				for (int i =0; i< headers.length; i++) {
					Header header1 = headers[i];
					headerMap.put(header1.getName(), header1.getValue());
				}
			}
			// 从响应对象中获取响应内容
			HttpEntity entity = httpResponse.getEntity();
			JSONObject retObj = new JSONObject();
			retObj.put("ResponseHeaders", new JSONObject(headerMap).toJSONString());
			retObj.put("ResponseBody", EntityUtils.toString(entity, (charset==null || charset.equals("")) ? "UTF-8" : charset));
			result = retObj.toJSONString();
			
			return result;
		} catch (Exception e) {
			logger.error("HttpClientUtil Request ---->>>:", e);
		} finally {
			if (null != httpResponse) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return "";
	}


	/**
	 * HTTPS请求添加信任
	 * */
	public HttpClient(String ssl) throws Exception{  
        super();
        //传输协议需要根据自己的判断　  
        SSLContext ctx = SSLContext.getInstance(ssl);  
        X509TrustManager tm = new X509TrustManager() {  
                @Override  
                public void checkClientTrusted(X509Certificate[] chain,  
                        String authType) throws CertificateException {  
                }  
                @Override  
                public void checkServerTrusted(X509Certificate[] chain,  
                        String authType) throws CertificateException {  
                }  
                @Override  
                public X509Certificate[] getAcceptedIssuers() {  
                    return null;  
                }  
        };  
        ctx.init(null, new TrustManager[]{tm}, null);  
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
    }
	
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Content-Type", "text");
		System.out.println(doPut("http://192.168.1.183:10099/http/rest/person/location/real", header, "asdf=asdf&fff=333", null));
//		System.out.println(doTrace("https://blog.csdn.net/ncuzengxiebo/article/details/82813900", null,  null));
	}
}