package com.common.HttpServer.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * @Title 业务处理器
 * @author GuoQ.yin
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

	private String uri;
	private String reqMth;
	private String sslFlag;
	private ScriptEngine engine;

	public NettyServerHandler(String uri, String reqMth, String sslFlag, ScriptEngine engine) {
		this.uri = uri;
		this.reqMth = reqMth;
		this.engine = engine;
		this.sslFlag = sslFlag;
	}

	
	//接收请求
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		logger.info("业务线程：" + Thread.currentThread().getId() + "---" + Thread.currentThread().getName());
		JSONObject result = new JSONObject();
		if (!(msg instanceof FullHttpRequest)) {
			result.put("code", 400);
			result.put("status", "error");
			result.put("data", "An unknown request!");
			send(ctx, JSON.toJSONString(result), HttpResponseStatus.BAD_REQUEST);
			return;
		}
		FullHttpRequest httpRequest = (FullHttpRequest) msg;
		// 判断请求数据大小
		if(httpRequest.headers().size()==0) {
			try {
				throw new Exception(" Too much data when the client requests it !!! ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			String path = httpRequest.uri(); // 获取路径
			String pathValid = path.split("[?]")[0];
			HttpMethod method = httpRequest.method();// 获取请求方法
			
			//针对ajax第一次请求校验
			if(method.toString().equals("OPTIONS")) {
				send(ctx, JSON.toJSONString(result), HttpResponseStatus.OK);
				return;
			}
			
			// 判断请求方法GET/POST
			if (!reqMth.toUpperCase().equals(method.toString())) {
				result.put("code", 403);
				result.put("status", "error");
				if (HttpMethod.GET.equals(method)) {
					result.put("data", "Request error, Server limit request method is：" + reqMth.toUpperCase()+ " !!，Parameters of the support:[ application/json OR x-www-form-urlencoded OR text/plain OR multipart/form-data ]");
					logger.error("Server limit request method is POST, But client request method is GET");
				}
				if (HttpMethod.POST.equals(method)) {
					result.put("data", "Request error, Server limit request method is：" + reqMth.toUpperCase() + " !!");
					logger.error("Server limit request method is GET, But client request method is POST");
				}
				send(ctx, JSON.toJSONString(result), HttpResponseStatus.FORBIDDEN);
				return;
			}

			// 校验请求路径是否正确
			if (!uri.equalsIgnoreCase(pathValid)) {
				result.put("code", 403);
				result.put("status", "error");
				result.put("data", "illegal request, Check the request path!!");
				send(ctx, JSON.toJSONString(result), HttpResponseStatus.FORBIDDEN);
				logger.error("illegal request, request path error!!");
				return;
			}

			// GET请求, 若有参数url直接传
			if (HttpMethod.GET.equals(method)) {
				String args = "";
				Object jsResult = null;
				String reqParam = getParams(httpRequest);
				if (reqParam.equals("{}")) {
					logger.info("GET Request, no parameters");
				} else {
					logger.info(" GET Request parameters---》" + reqParam);
					args = reqParam;
				}
				try {
					//engine.put("body", reqParam);
//					Invocable inv = (Invocable) engine;
//					jsResult = inv.invokeFunction("run", args);
					if (jsResult == null) {
						jsResult = "Request success";
					}
					result.put("code", 200);
					result.put("status", "success");
					result.put("data", jsResult);
				} catch (Exception exp) {
					try {
						logger.error("server javascript code error：", exp);
					} catch (Exception e) {
						logger.error(exp.getMessage());
						StackTraceElement[] stacks = exp.getStackTrace();
						String stackTraceMsg = "";
						if (stacks != null && stacks.length > 0) {
							for (StackTraceElement stackTrace : stacks) {
								stackTraceMsg = stackTraceMsg + stackTrace.getFileName() + "["
										+ stackTrace.getLineNumber() + "]\n";
							}
						}
						logger.error(stackTraceMsg);
					}
					result.put("code", 500);
					result.put("status", "error");
					result.put("data", "server javascript code error");
				}
				if (jsResult.equals("Request success")) {
					send(ctx, JSON.toJSONString(result), HttpResponseStatus.OK);
				}else {
					String retFormatResult = String.valueOf(jsResult).trim();
					if(retFormatResult.indexOf("CUSTOM(") == 0) {
						send(ctx, retFormatResult.substring(7, retFormatResult.length()-1), HttpResponseStatus.OK);
					}else {
						send(ctx, JSON.toJSONString(result), HttpResponseStatus.OK);
					}
				}
				return;
			}

			// POST请求,支持text/plain, application/json, x-www-form-urlencoded, multipart/form-data
			if (HttpMethod.POST.equals(method)) {
				String args = "";
				Object jsResult = null;
				String bodyParam = getJsonParams(httpRequest);
				if(httpRequest.headers().get("Content-Type") !=null) {
					String strContentType = httpRequest.headers().get("Content-Type").trim();
					if (!bodyParam.equals("")) {
						if (strContentType.contains("x-www-form-urlencoded")) {
							bodyParam = getFormParams(httpRequest);
							if (bodyParam.equals("{}")) {// POST参数校验
								result.put("code", 403);
								result.put("status", "error");
								result.put("data", "Request parameters are not standard, Parameter request format specified (x-www-form-urlencoded) Not consistent with the format of the parameters actually sent!!");
								send(ctx, JSON.toJSONString(result), HttpResponseStatus.FORBIDDEN);
								logger.error("Request parameters are not standard, Parameter request format specified (x-www-form-urlencoded) Not consistent with the format of the parameters actually sent!!");
								return;
							}
						} else if (strContentType.contains("text/plain") || strContentType.contains("text")) {
							bodyParam = getJsonParams(httpRequest);
						} else if (strContentType.contains("multipart/form-data")) {
							bodyParam = getMultPartParams(httpRequest);
						} else if (strContentType.contains("application/json")) {
							if (isJSONValid(bodyParam) == false) {// POST参数校验
								result.put("code", 403);
								result.put("status", "error");
								result.put("data", "Request parameters are not standard, Verify that the parameter is a standard JSON format string。");
								send(ctx, JSON.toJSONString(result), HttpResponseStatus.FORBIDDEN);
								logger.error("Request parameters are not standard, Verify that the parameter is a standard JSON format string。");
								return;
							}
						} else if (strContentType.contains("application/xml") || strContentType.contains("text/xml")) {
							bodyParam = getJsonParams(httpRequest); 
						} else {
							result.put("code", 505);
							result.put("status", "error");
							result.put("data","Exception request, Please use application/json OR x-www-form-urlencoded OR text/plain OR multipart/form-data request!!");
							send(ctx, JSON.toJSONString(result), HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED);
							logger.error("Exception request, Please use application/json OR x-www-form-urlencoded OR text/plain OR multipart/form-data request!!");
							return;
						}
						args = bodyParam;
					} else {
						logger.info("POST Request, no parameters");
					}
				} else {
					result.put("code", 403);
					result.put("status", "error");
					result.put("data", "Need Add headers, And add Content-Type property in headers, such as(Content-Type:application/json)");
					send(ctx, JSON.toJSONString(result), HttpResponseStatus.FORBIDDEN);
					logger.error("Client did not  hava headers, Need Add Content-Type property in headers , such as(Content-Type:application/json)");
					return;
				}
 				

				try {
					//engine.put("body", bodyParam);
//					Invocable inv = (Invocable) engine;
//					jsResult = inv.invokeFunction("run", args);
					if (jsResult == null) {
						jsResult = "Request success";
					}
					result.put("code", 200);
					result.put("status", "success");
					result.put("data", jsResult);

				} catch (Exception exp) {
					try {
						logger.error("server javascript code error：", exp);
					} catch (Exception e) {
						logger.error(exp.getMessage());
						StackTraceElement[] stacks = exp.getStackTrace();
						String stackTraceMsg = "";
						if (stacks != null && stacks.length > 0) {
							for (StackTraceElement stackTrace : stacks) {
								stackTraceMsg = stackTraceMsg + stackTrace.getFileName() + "["
										+ stackTrace.getLineNumber() + "]\n";
							}
						}
						logger.error(stackTraceMsg);
					}
					result.put("code", 500);
					result.put("status", "error");
					result.put("data", "server javascript code error");
				}
				if (jsResult.equals("Request success")) {
					send(ctx, JSON.toJSONString(result), HttpResponseStatus.OK);
				}else {
					String retFormatResult = String.valueOf(jsResult).trim();
					if(retFormatResult.indexOf("CUSTOM(") == 0) {
						send(ctx, retFormatResult.substring(7, retFormatResult.length()-1), HttpResponseStatus.OK);
					}else {
						send(ctx, JSON.toJSONString(result), HttpResponseStatus.OK);
					}
				}
				return;
			}
		} catch (Exception e) {
			result.put("code", 400);
			result.put("status", "error");
			result.put("data", "An unknown request，The request failed!!" + e);
			send(ctx, JSON.toJSONString(result), HttpResponseStatus.BAD_REQUEST);
			logger.error("An unknown request，The request failed!!");
			e.printStackTrace();
		} finally {
			httpRequest.release();// 释放请求
		}
	}
	
	/*
	 * 发送的返回值
	 * @param ctx 返回
	 * @param context 消息
	 * @param status 状态
	 */
	@SuppressWarnings("deprecation")
	private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
		if (context != null) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			//允许跨域访问
			response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
			response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS,"Origin, X-Requested-With, Content-Type, Accept");
			response.headers().set(ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, PUT, DELETE");
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	//建立连接时，返回消息
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Client IP:" + ctx.channel().remoteAddress());
		ctx.writeAndFlush("Client " + InetAddress.getLocalHost().getHostName() + " connect！ ");
		super.channelActive(ctx);
	}

	/** GET参数解析 **/
	private String getParams(FullHttpRequest request) {
		JSONObject params = new JSONObject();
		String uri = request.uri();
		QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
		Map<String, List<String>> uriAttributes = queryDecoder.parameters();
		for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
			for (String attrVal : attr.getValue()) {
				params.put(attr.getKey(), attrVal);
			}
		}
		return JSON.toJSONString(params);
	}

	/** POST参数解析 **/
	// JSON验证
	private final static boolean isJSONValid(String test) {
		try {
			JSONObject.parseObject(test);
		} catch (JSONException ex) {
			try {
				JSONObject.parseArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	// 解析from表单数据（Content-Type = x-www-form-urlencoded）
	private String getFormParams(FullHttpRequest fullHttpRequest) {
		JSONObject params = new JSONObject();
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
		List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

		for (InterfaceHttpData data : postData) {
			if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
				MemoryAttribute attribute = (MemoryAttribute) data;
				params.put(attribute.getName(), attribute.getValue());
			}
		}
		return JSON.toJSONString(params);
	}

	// 解析application/json与text/plain数据（Content-Type = application/json 与 text/plain）
	private String getJsonParams(FullHttpRequest request) {
		ByteBuf buf = request.content();
		return buf.toString(CharsetUtil.UTF_8);
	}

	// 解析multipart数据（Content-Type = multipart/form-data）
	private String getMultPartParams(FullHttpRequest request) {
		JSONObject params = new JSONObject();
		try {
			File dir = new File("./fileCache/");
			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			}
			File directory = new File("");
			DiskFileUpload.baseDirectory = directory.getCanonicalPath() + "/fileCache/";
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
			List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

			for (InterfaceHttpData data : postData) {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) data;
					params.put(attribute.getName(), attribute.getValue());// 普通参数直接返回
				} else if (data.getHttpDataType() == HttpDataType.FileUpload) {
					FileUpload fileUpload = (FileUpload) data;
					String fileName = fileUpload.getFilename();
					StringBuffer fileNameBuf = new StringBuffer();
					if (fileUpload.isCompleted()) {
						fileNameBuf.append(DiskFileUpload.baseDirectory).append(fileName);// 文件缓存到本地
						fileUpload.renameTo(new File(fileNameBuf.toString()));
						params.put("FileName:" + fileName, "Absolute path:" + fileNameBuf.toString());
					}
				}
			}
		} catch (Exception e) {
			logger.error("multparts/from-data Parameter resolution failed" + e);
		}
		return JSON.toJSONString(params);
	}
}
