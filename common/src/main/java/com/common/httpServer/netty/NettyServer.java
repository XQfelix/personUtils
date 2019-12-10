package com.common.httpServer.netty;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * @Title 服务启动
 * @author GuoQ.yin
 * */
public class NettyServer{
	
	private static Logger logger = LoggerFactory.getLogger(NettyServer.class);
	
    private static EventLoopGroup group = new NioEventLoopGroup();   // NIO模式
    private static EventLoopGroup workGroup = new NioEventLoopGroup();
    private static ServerBootstrap b = new ServerBootstrap();
   
    private int port;
    private String url;
	private String reqMth;
	private String script;
	private String sslFlag;
    private ScriptEngine engine;
    
    
    public NettyServer(String url,int port, String reqMth, String sslFlag) {
		this.url = url;
	    this.port = port;
	    this.reqMth = reqMth;
	    this.sslFlag = sslFlag;
	}
	

    
    public void ServerStart() throws InterruptedException, ScriptException{
    	try {
    		ScriptEngineManager factory = new ScriptEngineManager();
//    		engine = factory.getEngineByName("nashorn");
//    		engine.put("heartBeat", heartBeatTask);
//    		engine.put("logger", logger);
//    		DiCMDBAdapter cmdbAdapter = DiCMDBAdapter.getInstance();
//    		engine.put("tarsierTool", cmdbAdapter);
//    		engine.eval(script+JavascriptTool.getJsTool());
    		
            b.group(group, workGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new NettyServerFilter(url, reqMth, sslFlag, engine)); //设置过滤器
            
            ChannelFuture f = b.bind(port).sync();//端口监听
            logger.info("HTTP service Bootup successful , path: " + url + ":" + port);
            
            f.channel().closeFuture().sync();// 监听服务器关闭监听
        } finally {
            group.shutdownGracefully(); //关闭EventLoopGroup，释放掉所有资源包括创建的线程  
        }
    }
}
