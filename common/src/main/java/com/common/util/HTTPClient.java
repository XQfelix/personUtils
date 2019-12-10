package com.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;


public class HTTPClient {

	private static Logger logger = LoggerFactory.getLogger(HTTPClient.class);


	/**
	 * 通过http请求调用webservice
	 *
	 * @param wsdl 请求的wsdl地址（不包含?wsdl）
	 * @param xmlParam soap请求的参数
	 * @param result 返回结果中的指定返回的结果标识
	 * @return
	 * @throws Exception
	 */
	public  static String httpCallWsdl(String wsdl,String xmlParam,String result){
		//拼接参数
		String soapResponseData = "";
		//拼接SOAP
		StringBuffer soapRequestData = new StringBuffer("");
		soapRequestData.append(xmlParam);

		PostMethod postMethod = new PostMethod(wsdl);
		// 然后把Soap请求数据添加到PostMethod中
		byte[] b=null;
		InputStream is=null;
		try {
			b = soapRequestData.toString().getBytes("utf-8");
			is = new ByteArrayInputStream(b, 0, b.length);
			RequestEntity re = new InputStreamRequestEntity(is, b.length,"text/xml; charset=UTF-8");
			postMethod.setRequestEntity(re);
			HttpClient httpClient = new HttpClient();
			int status = httpClient.executeMethod(postMethod);
			logger.info("httpCallWsdl 返回状态码:"+status);
			if(status==200){
				soapResponseData = getMesage(postMethod.getResponseBodyAsString(),result);
			}  else {
				logger.error(">>>返回状态码:"+status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					logger.error("关闭字节流异常，",e);
				}
			}
		}
		return soapResponseData;
	}

	private static String getMesage(String soapAttachment,String result){
		if(result==null){
			return null;
		}
		if(soapAttachment!=null && soapAttachment.length()>0){
			int begin = soapAttachment.indexOf(result);
			begin = soapAttachment.indexOf(">", begin);
			//int end = soapAttachment.indexOf("</"+result+">");
			String str = soapAttachment.substring(begin+1);
			str = str.replaceAll("<", "<");
			str = str.replaceAll(">", ">");

			String res = str.toString();
			//将数据转换为json格式
//	            if(str.toString().indexOf("<")==0) {
//					XMLSerializer xmlSerializer = new XMLSerializer();
//					net.sf.json.JSON dataJSON = xmlSerializer.read(str.toString());
//					res = dataJSON.toString();
//				}else if(str.toString().indexOf("{")==0){
//					res = str.toString();
//				}
			return res;
		}else{
			return "";
		}
	}




	/**
	 * 发送httpPost请求(支持Content-Type: application/x-www-form-urlencoded)
	 *
	 * @param url 请求的地址
	 * @param headers 请求头
	 * @param paramMap 请求参数
	 * @param charset 编码集默认UTF-8
	 * @return String
	 */
	public static String httpFormPost(String url, Map<String,Object> headers, Map<String, Object> paramMap, String charset) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		String result = "";
		// 创建httpClient实例
		httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		// 配置请求参数实例
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(120000)// 设置连接主机服务超时时间
				.setConnectionRequestTimeout(120000)// 设置连接请求超时时间
				.setSocketTimeout(120000)// 设置读取数据连接超时时间
				.build();
		// 为httpPost实例设置配置
		httpPost.setConfig(requestConfig);
		// 设置请求头
		if(headers!=null&&headers.size()>0){
			for(String hKey:headers.keySet()){
				httpPost.addHeader(hKey, headers.get(hKey).toString());
			}
		}
		if (null != paramMap && paramMap.size() > 0) {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Set<Entry<String, Object>> entrySet = paramMap.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> mapEntry = iterator.next();
				nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
			}
			try {
				if(charset==null||charset.equals("")){
					charset = "UTF-8";
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		try {
			// httpClient对象执行post请求,并返回响应参数对象
			httpResponse = httpClient.execute(httpPost);
			// 从响应对象中获取响应内容
			HttpEntity entity = httpResponse.getEntity();
			result = EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != httpResponse) {
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
	 * 通用http请求
	 * @param url
	 * @param header
	 * @param param     除了Content-Type是text/plain，param的数据结构是String，其他param数据结构都是Map
	 * @param uploadFilePaths 当Content-Type是multipart/form-data时，需要上传文件绝对路径
	 * @param charset
	 * @return
	 */
	public static String httpCommon(String url, String requestMethod, Map<String, Object> header, Object param, List<String> uploadFilePaths, String charset) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		String result = null;
		// 创建httpClient实例
		httpClient = HttpClients.createDefault();
		// 配置请求参数实例
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(120000)// 设置连接主机服务超时时间
				.setConnectionRequestTimeout(120000)// 设置连接请求超时时间
				.setSocketTimeout(120000)// 设置读取数据连接超时时间
				.build();
		if("GET".equalsIgnoreCase(requestMethod)){
			HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			// 设置请求头  multipart/form-data不需要设置请求头
			if(header != null && header.size() > 0){
				header.forEach((key, value)->{
					httpGet.addHeader(key, value.toString());
				});
			}
			try {
				httpResponse = httpClient.execute(httpGet);
				// 从响应对象中获取响应内容
				HttpEntity entity = httpResponse.getEntity();
				result = EntityUtils.toString(entity, charset);
			} catch (IOException e) {
				logger.error("GET请求异常: ", e);
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
		}else {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			// 设置请求头  multipart/form-data不需要设置请求头
			if (header != null && header.size() > 0 && !(ContentType.MULTIPART_FORM_DATA.getMimeType().contains(String.valueOf(header.get("Content-Type"))) ||
					String.valueOf(header.get("Content-Type")).contains(ContentType.MULTIPART_FORM_DATA.getMimeType()))) {
				header.forEach((key, value) -> {
					httpPost.addHeader(key, value.toString());
				});
			}
			if (null != param) {
				String contentType = String.valueOf(header.get("Content-Type"));
				if (contentType == null) {
					logger.debug("Content-Type为空，使用默认值：application/json");
					contentType = "application/json";
				}
				if (charset == null || charset.equals("")) {
					charset = "UTF-8";
				}
				if(ContentType.APPLICATION_JSON.getMimeType().contains(contentType)){
					param = (Map<String, Object>)param;
					httpPost.setEntity(new StringEntity(JSON.toJSONString(param), ContentType.create("application/json", charset)));
				}else if (ContentType.APPLICATION_FORM_URLENCODED.getMimeType().contains(contentType)) {
					Map<String, Object> paramTemp = (Map<String, Object>)param;
					List<NameValuePair> nvp = new ArrayList<NameValuePair>();
					paramTemp.forEach((key, value) -> {
						nvp.add(new BasicNameValuePair(key, value.toString()));
					});
					try {
						httpPost.setEntity(new UrlEncodedFormEntity(nvp, charset));
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(), e);
					}
				}else if(ContentType.MULTIPART_FORM_DATA.getMimeType().contains(contentType) ||
						contentType.contains(ContentType.MULTIPART_FORM_DATA.getMimeType())){
					Map<String, Object> paramTemp = (Map<String, Object>)param;
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					Map<String, ContentBody> reqParam = new HashMap<>();
					paramTemp.forEach((key, value) -> {
						reqParam.put(key, new StringBody(value.toString(), ContentType.MULTIPART_FORM_DATA));
					});
					uploadFilePaths.forEach(key -> {
						File file = new File(key);
						builder.addPart(file.getName(), new FileBody(file));
					});
					reqParam.forEach((key, value) -> {
						builder.addPart(key, value);
					});
					builder.setCharset(Charset.forName(charset));
					httpPost.setEntity(builder.build());
				}else if(ContentType.TEXT_PLAIN.getMimeType().contains(contentType)){
					httpPost.setEntity(new StringEntity(param.toString(), ContentType.create("text/plain", charset)));
				}else if(ContentType.APPLICATION_XML.getMimeType().contains(contentType)){
					httpPost.setEntity(new StringEntity(param.toString(), ContentType.APPLICATION_XML));
				}
			}
			try {
				// httpClient对象执行post请求,并返回响应参数对象
				httpResponse = httpClient.execute(httpPost);
				// 从响应对象中获取响应内容
				HttpEntity entity = httpResponse.getEntity();
				result = EntityUtils.toString(entity, charset);
			} catch (Exception e) {
				logger.error("POST请求异常:", e);
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
		}
		return result;
	}


//	public static void main(String[] args) {
//		String wsdl ="http://www.webxml.com.cn/WebServices/WeatherWebService.asmx";
//		StringBuffer sb = new StringBuffer();
//		sb.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:web=\"http://WebXml.com.cn/\">");
//		sb.append("<soap:Header/>");
//		sb.append("<soap:Body>");
//		sb.append("<web:getSupportCity>");
//		sb.append("<web:byProvinceName>山西</web:byProvinceName>");
//		sb.append("</web:getSupportCity>");
//		sb.append("</soap:Body>");
//		sb.append("</soap:Envelope>");
//		String data = httpCallWsdl(wsdl, sb.toString(), "return");
//		System.out.println("获取到的数据为---->" + data);

//	}


}
