package com.common.util.Redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/14 13:46
 */
public class RedisUtil {
    private static Logger loggger = LoggerFactory.getLogger(RedisUtil.class);

    private static RedisUtil redisUtil = null;
    private static String addr;    //服务器IP地址
    private static int port;    //端口
    private static String auth = null;   //密码
    private static JedisPool jedisPool = null;
    private static int MAX_ACTIVE = 1024;    //连接实例的最大连接数
    private static int MAX_IDLE = 200;    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_WAIT = 10000;    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。
    private static int TIMEOUT = 10000;    //连接超时的时间　　
    private static boolean TEST_ON_BORROW = true;    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    public static final int DEFAULT_DATABASE = 0;    //数据库模式是16个数据库 0~15



    /**
     * @param addr redis地址
     * @param port redis端口
     * @param password redis密码, 若无传 null
     * */
    public static RedisUtil getInstance(String addr, int port, String password){
        if(jedisPool==null){
            if(auth!=null){
                redisUtil = new RedisUtil(addr, port);
            }else{
                redisUtil = new RedisUtil(addr, port, password);
            }
        }
        return redisUtil;
    }

    public RedisUtil(String addr, int port){
        this.addr = addr;
        this.port = port;
        init();
    }

    public RedisUtil(String addr, int port, String auth){
        this.addr = addr;
        this.port = port;
        this.auth = auth;
        init();
    }

    public void init(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if(auth==null){
            jedisPool = new JedisPool(config, addr, port, TIMEOUT);
        }else{
            jedisPool = new JedisPool(config, addr, port,TIMEOUT, auth);
        }
    }


    public static void main(String[] args) {
        RedisUtil redisUtil = getInstance("192.168.1.162", 6379, null);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 500000; i++) {
            JSONObject obj = new JSONObject();
            obj.put("name", "name"+i);
            obj.put("age", i);
            obj.put("sex", 1);
            jsonArray.add(obj);
        }

        System.out.println(redisUtil.saveList("te2", jsonArray));

        //jedis 实现分布式锁
//        Jedis jes = jedisPool.getResource();
//        jes.del("lock");
//        jes.setnx("lock", "1");
//        jes.setnx("lock", "1");
    }


    /**
     * @param key
     * */
    public List<String> getList(String key){
        Jedis jedis = jedisPool.getResource();
        List<String> list = jedis.lrange(key, 0, jedis.llen(key));
        return list;
    }



    /**
     * @param listKey key键
     * @param jsonArray 保存的数据
     * */
    public String saveList(String listKey, JSONArray jsonArray){
        Jedis jedis = jedisPool.getResource();
        String ret = "";
        try {
            long inLen = jsonArray.size();
            long beforeLen = jedis.llen(listKey);
            long startTime = new Date().getTime();
            for (Object obj:jsonArray) {
                jedis.lpush(listKey, JSON.toJSONString(obj));
            }
            long afterLen = jedis.llen(listKey);
            long endTime = new Date().getTime();
            if(afterLen>0 && afterLen-beforeLen==inLen){
                ret = " save num:[ " + inLen + " ]  spend time:[ " + (endTime-startTime) + "ms ]";
            }else{
                ret = "Save Failure!!";
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return ret;
    }


    /**
     * 返回jedis对象
     * 使用完切记要close()
     * */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }




}
