package com.common.httpServer;

import com.common.httpServer.netty.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 11:42
 */
public class HttpMain {
    private static Logger logger = LoggerFactory.getLogger(HttpMain.class);


    public static void main(String[] args) {
        try {
            NettyServer NS = new NettyServer("/rest", 8899, "POST", "false");
            NS.ServerStart();
        }  catch (Exception e) {
            logger.error("HTTP service startup error:",e);
            System.exit(-1);
        }
    }
}
