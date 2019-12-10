package com.common.httpServer.netty;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.SystemPropertyUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.script.ScriptEngine;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * @Title 过滤器
 * @author GuoQ.yin
 * */
public class NettyServerFilter extends ChannelInitializer<SocketChannel> {
	private String uri;
	private String reqMth;
	private String sslFlag;
	private ScriptEngine engine;
	EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(Math.max(256, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 4)));
	public NettyServerFilter(String uri, String reqMth, String sslFlag, ScriptEngine engine) {
		this.uri = uri;
		this.reqMth = reqMth;
		this.sslFlag = sslFlag;
		this.engine = engine;
	}
	
	@Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        if(sslFlag.toLowerCase().equals("true")){
        	//添加sslhandler
    	    char[] passArray = "dix@Uinnova".toCharArray(); //jks密码
    	    SSLContext sslContext = SSLContext.getInstance("TLSv1");
    	    KeyStore ks = KeyStore.getInstance("JKS");
    	    //加载keytool 生成的文件
    	    FileInputStream inputStream = new FileInputStream(System.getProperty("user.dir")+"/conf/dixssl.jks");
    	    ks.load(inputStream, passArray);
    	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    	    kmf.init(ks, passArray);
    	    sslContext.init(kmf.getKeyManagers(), null, null);
    	    inputStream.close();
    	    SSLEngine sslEngine = sslContext.createSSLEngine(); 
    	    sslEngine.setUseClientMode(false);
    	    ph.addLast(new SslHandler(sslEngine));
        }
        ph.addLast("encoder",new HttpResponseEncoder());
        ph.addLast("decoder",new HttpRequestDecoder());
        ph.addLast("aggregator", new HttpObjectAggregator(500*1024*1024)); //500M
        ph.addLast(businessGroup, "handler", new NettyServerHandler(uri, reqMth, sslFlag, engine));// 服务端业务逻辑
    }
}
