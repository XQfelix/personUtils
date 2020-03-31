package com.common.util.JedisUtil;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.*;

import java.util.*;


/**
 * @author GQ.Yin
 * @version 1.0
 * @title Redis单例工具类
 * @date 2019/12/14 16:17
 */
public class RedisSingleUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisSingleUtil.class);
    private static RedisSingleUtil redisSingleUtil = null;
    private static JedisPool jedisPool = null;


    public static void main(String[] args) {
        RedisSingleUtil redisSingleUtil = getInstance("127.0.0.1:6379");
//        redisSingleUtil.saveString("1233", "123", "3");
        System.out.println(redisSingleUtil.hashGetAll("tree:root"));
        Jedis jedis = redisSingleUtil.getJedis();
    }



    /**
     * @param address redis地址(0.0.0.0:0000)
     * @param password 密码
     * */
    public static RedisSingleUtil getInstance(String address, String... password){
        if(redisSingleUtil == null){
            redisSingleUtil = new RedisSingleUtil();
            if(password.length > 0){
                redisSingleUtil.init(address, password[0]);
            }else{
                redisSingleUtil.init(address, null);
            }
        }
        return redisSingleUtil;
    }


    private void init(String address, String password){
        JedisPoolConfig config = new JedisPoolConfig();
        //最大连接数, 默认8个
        config.setMaxTotal(1000);
        //大空闲连接数, 默认8个
        config.setMaxIdle(10);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(3000);
        //最小空闲连接数, 默认0
        config.setMinIdle(0);
        //是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(true);
        //是否启用后进先出, 默认true
        config.setLifo(true);
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(false);
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(false);

        String[] clusters = address.split(",");
        String ip = clusters[0].split(":")[0];
        int port = Integer.parseInt(clusters[0].split(":")[1]);
        if (password == null || password.equals("")) {
            jedisPool = new JedisPool(config, ip, port, 5000);
        }else{
            jedisPool = new JedisPool(config, ip, port, 5000, password);
        }
    }


    /**
     * 返回Jedis对象
     *
     * @param index 数据库(默认0)
     * */
    public Jedis getJedis(String... index){
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis;
    }


    /**
     * 将数据存入缓存
     *
     * @param key
     * @param val
     * @return
     */
    private void saveString(String key, String val, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.set(key, val);
    }

    /**
     * 将数据存入缓存的集合中
     *
     * @param key
     * @param val
     * @return
     */
    private void saveToSet(String key, String val, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.sadd(key, val);
    }

    /**
     * 将 key的值保存为 value ，当且仅当 key 不存在。 若给定的 key 已经存在，则 SETNX 不做任何动作。 SETNX 是『SET
     * if Not eXists』(如果不存在，则 SET)的简写。
     * 保存成功，返回 true
     * 保存失败，返回 false
     *
     * @param key
     * @param val
     * @return
     */
    private boolean saveNX(String key, String val, String... index) {
        /** 设置成功，返回 1 设置失败，返回 0 **/
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return (jedis.setnx(key, val).intValue() == 1);
    }

    /**
     * 将 key的值保存为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。 SETNX 是『SETif Not eXists』(如果不存在，则 SET)的简写。 <br>
     * 保存成功，返回 true <br>
     * 保存失败，返回 false
     *
     * @param key
     * @param val
     * @param expire 超时时间
     * @return 保存成功，返回 true 否则返回 false
     */
    private boolean saveNX(String key, String val, int expire, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        boolean ret = (jedis.setnx(key, val).intValue() == 1);
        if (ret) {
            jedis.expire(key, expire);
        }
        return ret;
    }

    /**
     * 将数据存入缓存（并设置失效时间）
     *
     * @param key
     * @param val
     * @param seconds
     * @return
     */
    private void saveString(String key, String val, int seconds, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.set(key, val);
        jedis.expire(key, seconds);
    }

    /**
     * 将自增变量存入缓存
     */
    private void saveSeq(String key, long seqNo, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.del(key);
        jedis.incrBy(key, seqNo);
    }

    /**
     * 将递增浮点数存入缓存
     */
    private void saveFloat(String key, float data, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.del(key);
        jedis.incrByFloat(key, data);
    }

    /**
     * 保存复杂类型数据到缓存
     *
     * @param key
     * @param obj
     * @return
     */
    private void saveBean(String key, Object obj, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.set(key, JSONObject.toJSONString(obj));
    }

    /**
     * 保存复杂类型数据到缓存（并设置失效时间）
     *
     * @param key
     * @param obj
     * @param seconds
     * @return
     */
    private void saveBean(String key, Object obj, int seconds, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.set(key, JSONObject.toJSONString(obj));
        jedis.expire(key, seconds);
    }

    /**
     * 存到指定的队列中
     *
     * @param key
     * @param val
     * @param size 队列大小限制 0：不限制
     */
    private void saveToQueue(String key, String val, long size, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        if (size > 0 && jedis.llen(key) >= size) {
            jedis.rpop(key);
        }
        jedis.lpush(key, val);
    }

    /**
     * 保存到hash集合中
     *
     * @param hName 集合名
     * @param key
     * @param value
     */
    private void hashSet(String hName, String key, String value, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.hset(hName, key, value);
    }


    /**
     * 保存到hash集合中
     *
     * @param hName
     * @param key
     * @param t
     * @param <T>
     */
    private <T> void hashSet(String hName, String key, T t, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.hset(hName, key, JSONObject.toJSONString(t));
    }

    /**
     * 取得复杂类型数据
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */

    private <T> T getBean(String key, Class<T> clazz, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        String value = jedis.get(key);
        if (value == null) {
            return null;
        }
        return JSONObject.parseObject(value, clazz);
    }

    /**
     * 从缓存中取得字符串数据
     *
     * @param key
     * @return 数据
     */
    private String getString(String key, String... index) {
        // 暂时从缓存中取得
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.get(key);
    }

    /**
     * 从指定队列里取得数据
     *
     * @param key
     * @param size 数据长度
     * @return
     */
    private List<String> getFromQueue(String key, long size, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        if (!jedis.exists(key)) {
            return new ArrayList<String>();
        }
        if (size > 0) {
            return jedis.lrange(key, 0, size - 1);
        } else {
            return jedis.lrange(key, 0, jedis.llen(key) - 1);
        }
    }

    /**
     * 功能: 从指定队列里取得数据<br />
     *
     * @param key
     * @return
     */
    private String popQueue(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        if (!jedis.exists(key)) {
            return null;
        }
        return jedis.rpop(key);
    }

    /**
     * 取得序列值的下一个
     *
     * @param key
     * @return
     */
    private long getSeqNext(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.incr(key);
    }

    /**
     * 取得序列值的下一个
     *
     * @param key
     * @return
     */
    private long getSeqNext(String key, long by, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.incrBy(key, by);
    }

    /**
     * 将序列值回退一个
     *
     * @param key
     * @return
     */
    private void getSeqBack(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.decr(key);
    }

    /**
     * 从hash集合里取得
     *
     * @param hName
     * @param key
     * @return
     */
    private String hashGet(String hName, String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.hget(hName, key);
    }

    /**
     * 从hash集合里取得
     *
     * @param hName
     * @param key
     * @return
     */
    private <T> T hashGet(String hName, String key, Class<T> clazz, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        String value = jedis.hget(hName, key);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return JSONObject.parseObject(value, clazz);
    }

    /**
     * 增加浮点数的值
     *
     * @param key
     * @return
     */
    private float incrFloat(String key, float incrBy, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.incrByFloat(key, incrBy).floatValue();
    }

    /**
     * 判断是否缓存了数据
     *
     * @param key 数据KEY
     * @return 判断是否缓存了
     */
    private boolean isCached(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.exists(key);
    }

    /**
     * 判断hash集合中是否缓存了数据
     *
     * @param hName
     * @param key   数据KEY
     * @return 判断是否缓存了
     */
    private boolean hashCached(String hName, String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.hexists(hName, key);
    }

    /**
     * 判断是否缓存在指定的集合中
     *
     * @param key 数据KEY
     * @param val 数据
     * @return 判断是否缓存了
     */
    private boolean isMember(String key, String val, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.sismember(key, val);
    }

    /**
     * 从缓存中删除数据
     *
     * @param key
     * @return
     */
    private String delString(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        String str = jedis.get(key);
        jedis.del(key);
        return str;
    }

    /**
     * 从缓存中删除数据
     *
     * @param key
     * @return
     */
    private void delKey(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.del(key);
    }

    /**
     * 从缓存中删除复杂数据
     *
     * @param key
     * @param clazz
     * @param <E>
     * @return
     */
    private <E> E delBean(String key, Class<E> clazz, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        E obj = getBean(key, clazz);
        jedis.del(key.getBytes());
        return obj;
    }

    /**
     * 设置超时时间
     *
     * @param key
     * @param seconds
     */
    private void expire(String key, int seconds, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.expire(key, seconds);
    }

    /**
     * 列出set中所有成员
     *
     * @param setName set名
     * @return
     */
    private Set<String> listSet(String setName, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.smembers(setName);

    }

    /**
     * 向set中追加一个值
     *
     * @param setName set名
     * @param value
     */
    private void setSave(String setName, String value, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.sadd(setName, value);
    }

    /**
     * 逆序列出sorted set包括分数的set列表
     *
     * @param key   set名
     * @param start 开始位置
     * @param end   结束位置
     * @return 列表
     */
    private Set<Tuple> listSortedsetRev(String key, int start, int end, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.zrevrangeWithScores(key, start, end);
    }

    /**
     * 逆序取得sorted sort排名
     *
     * @param key    set名
     * @param member 成员名
     * @return 排名
     */
    private Long getRankRev(String key, String member, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.zrevrank(key, member);
    }

    /**
     * 根据成员名取得sorted sort分数
     *
     * @param key    set名
     * @param member 成员名
     * @return 分数
     */
    private Double getMemberScore(String key, String member, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.zscore(key, member);
    }

    /**
     * 向sorted set中追加一个值
     *
     * @param key    set名
     * @param score  分数
     * @param member 成员名称
     */
    private void saveToSortedset(String key, Double score, String member, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.zadd(key, score, member);
    }

    /**
     * 从sorted set删除一个值
     *
     * @param key    set名
     * @param member 成员名称
     */
    private void delFromSortedset(String key, String member, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.zrem(key, member);
    }

    /**
     * 从hash map中取得复杂类型数据
     *
     * @param key
     * @param field
     * @param clazz
     */
    private <T> T getBeanFromMap(String key, String field, Class<T> clazz, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        String value = jedis.hget(key, field);
        if (value == null) {
            return null;
        }
        return JSONObject.parseObject(value, clazz);
    }

    /**
     * 从hashmap中删除一个值
     *
     * @param key   map名
     * @param field 成员名称
     */
    private void delFromMap(String key, String field, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        jedis.hdel(key, field);
    }

    /**
     * 功能: 从hash中取得全部key对应所有field
     *
     * @param key hash集的名称
     * @return
     */
    private Map<String, String> hashGetAll(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.hgetAll(key);
    }


    /**
     * 根据key增长 ，计数器
     *
     * @param key
     * @return
     */
    private long incr(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.incr(key);
    }

    /**
     * 根据key获取当前计数结果
     *
     * @param key
     * @return
     */
    private String getCount(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.get(key);
    }

    /**
     * 将所有指定的值插入到存于 key 的列表的头部。如果 key 不存在，那么在进行 push 操作前会创建一个空列表
     *
     * @param <T>
     * @param key
     * @param value
     * @return
     */
    private <T> Long lpush(String key, T value, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.lpush(key, JSONObject.toJSONString(value));
    }

    /**
     * 只有当 key 已经存在并且存着一个 list 的时候，在这个 key 下面的 list 的头部插入 value。 与 LPUSH 相反，当
     * key 不存在的时候不会进行任何操作
     *
     * @param key
     * @param value
     * @return
     */
    private <T> Long lpushx(String key, T value, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.lpushx(key, JSONObject.toJSONString(value));
    }

    /**
     * 返回存储在 key 里的list的长度。 如果 key 不存在，那么就被看作是空list，并且返回长度为 0
     *
     * @param key
     * @return
     */
    private Long llen(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.llen(key);
    }

    /**
     * (返回复杂对象)返回存储在 key 的列表里指定范围内的元素。 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推
     *
     * @param <T>
     * @param key
     * @return
     */
    private <T> List<T> lrange(String key, long start, long end, Class<T> clazz, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        List<String> list = jedis.lrange(key, start, end);
        if (list == null) {
            return null;
        }
        List<T> ts = new ArrayList<T>(list.size());
        for (String s : list) {
            ts.add(JSONObject.parseObject(s, clazz));
        }
        return ts;
    }

    /**
     * 返回存储在 key 的列表里指定范围内的元素。 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推
     *
     * @param key
     * @return
     */
    private List<String> lrange(String key, long start, long end, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.lrange(key, start, end);
    }

    /**
     * 移除并且返回 key 对应的 list 的第一个元素
     *
     * @param key
     * @return
     */
    private String lpop(String key, String... index) {
        Jedis jedis = jedisPool.getResource();
        if (index.length > 0) {
            jedis.select(Integer.parseInt(index[0]));
        }
        return jedis.lpop(key);
    }

    /**
     * 获取所有匹配的key
     *
     * @param pattern 表达式 (2016* 匹配所有2016的key)
     * @return
     */
    private TreeSet<String> getAllKeys(String pattern, String... index) {
        logger.info("Start getting keys :{}...", pattern);
        TreeSet<String> keys = new TreeSet<String>();
        try (Jedis jedis = jedisPool.getResource()) {
            keys.addAll(jedis.keys(pattern));
        } catch (Exception e) {
            logger.error("Getting keys error: {}", e);
        }
        logger.info("Keys gotten!");
        return keys;
    }
}