package com.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: GuoQ.Yin
 * Date: 2020-03-09
 * Description: PingUtil
 */
public class PingUtil {
    private static final Logger logger = LoggerFactory.getLogger(PingUtil.class);


    /**
     * @Description Ping一次
     *
     * @param ipAddress  需要ping的IP地址
     * return boolean
     * */
    public static boolean ping(String ipAddress) throws Exception {
        int  timeOut =  3000 ;  //超时应该在3钞以上
        boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);     // 当返回值是true时，说明host是可用的，false则不可。
        return status;
    }

    /**
     * @Description Ping多次
     *
     * @param ipAddress 需要ping的IP地址
     * @param pingTimes ping的次数
     * @param timeOut 超时间隔
     * */
    public static boolean ping(String ipAddress, Integer pingTimes, Integer timeOut) {
        BufferedReader in = null;
        Runtime r = Runtime.getRuntime();  // 将要执行的ping命令,此命令是windows格式的命令
        if(null == pingTimes){
            pingTimes = 3;
        }
        if(null == timeOut){
            timeOut = 3000;
        }
        String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;
        try {   // 执行命令并获取输出
            logger.info("{}", pingCommand);
            Process p = r.exec(pingCommand);
            if (p == null) {
                return false;
            }
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));   // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
            int connectedCount = 0;
            String line = null;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResult(line);
            }   // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
            return connectedCount == pingTimes;
        } catch (Exception ex) {
            ex.printStackTrace();   // 出现异常则返回假
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getCheckResult(String line) {
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",    Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        String ipAddress = "127.0.0.1";
        System.out.println(ping(ipAddress));
        System.out.println(ping(ipAddress, 3, 3000));
    }
}
