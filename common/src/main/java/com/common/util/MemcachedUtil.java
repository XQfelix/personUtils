package com.common.util;


import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/14 16:47
 */
public class MemcachedUtil {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        MemcachedClient mcc = new MemcachedClient(new InetSocketAddress("192.168.1.162", 11211));
        Future fo = mcc.set("memcached", 10, "memcached");// 添加数据
        System.out.println("set status:" + fo.get());// 输出执行set方法后的状态
        System.out.println("get - " + mcc.get("memcached"));// 使用get方法获取数据

        fo = mcc.add("memcached", 10, "add");// 添加
        System.out.println("add status:" + fo.get());// 输出执行add方法后的状态
        System.out.println("add - " + mcc.get("memcached"));// 获取键对应的值

        fo = mcc.add("memcached", 10, "add");// 添加
        System.out.println("add status:" + fo.get());// 输出执行add方法后的状态
        System.out.println("add - " + mcc.get("memcached"));// 获取键对应的值

        fo = mcc.replace("memcached", 10, "memcached replace");
        System.out.println("replace status:" + fo.get());// 输出执行replace方法后的状态
        System.out.println("replace - " + mcc.get("memcached"));// 获取键对应的值

        fo = mcc.append("memcached", " append");// 对存在的key进行数据添加操作
        System.out.println("append status:" + fo.get());// 输出执行 append方法后的状态
        System.out.println("append - " + mcc.get("memcached"));// 获取键对应的值

        fo = mcc.prepend("memcached", "prepend ");// 对存在的key进行数据添加操作
        System.out.println("prepend status:" + fo.get());// 输出执行prepend方法后的状态
        System.out.println("prepend - " + mcc.get("memcached"));// 获取键对应的值

        CASValue casValue = mcc.gets("memcached");// 通过 gets 方法获取 CAS token（令牌）
        System.out.println("CAS token - " + casValue);// 输出 CAS token（令牌） 值
        CASResponse casresp = mcc.cas("memcached", casValue.getCas(), " CAS");// 尝试使用cas方法来更新数据
        System.out.println("CAS Response - " + casresp);// 输出 CAS 响应信息
        System.out.println("CAS - " + mcc.get("memcached"));// 输出值

        fo = mcc.delete("memcached");// 对存在的key进行数据添加操作
        System.out.println("delete status:" + fo.get());// 输出执行 delete方法后的状态
        System.out.println("delete - " + mcc.get("memcached"));// 获取键对应的值

        Future number = mcc.set("number", 10, "1000");// 添加数字值
        System.out.println("set status:" + number.get());// 输出执行 set 方法后的状态
        System.out.println("incr - " + mcc.incr("number", 100));// 自增并输出
        System.out.println("decr - " + mcc.decr("number", 101));// 自减并输出

        //获取 统计信息例如 PID(进程号)、版本号、连接数等
        System.out.println(mcc.getStats());

        // 关闭连接
        mcc.shutdown();
    }

}
