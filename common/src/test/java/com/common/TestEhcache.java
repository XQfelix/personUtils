package com.common;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/21 15:15
 */
public class TestEhcache {
    @Test
    public void test1() {
        // 1. 创建缓存管理器(通过读取xml配置文件获取配置)
        CacheManager cacheManager = CacheManager.create("./src/main/resources/ehcache.xml");
        //或者通过编程式实现
//        Cache cache = manager.getCach e("catchname");
//        CacheConfiguration config = cache.getCacheConfiguration();
//        config.setTimeToIdleSeconds(60);
//        config.setTimeToLiveSeconds(120);
//        config.setmaxEntriesLocalHeap(10000);
//        config.setmaxEntriesLocalDisk(1000000);



        // 2. 获取缓存对象
        Cache cache = cacheManager.getCache("HelloWorldCache");

        // 3. 创建元素
        Element element = new Element("key1", "value1");

        // 4. 将元素添加到缓存
        cache.put(element);

        // 5. 获取缓存
        Element value = cache.get("key1");
        System.out.println("--------" + value);
        System.out.println(value.getObjectValue());

        // 6. 删除元素
        cache.remove("key1");

        Map<String, Object> obj = new HashMap<>();
        obj.put("hello", "world");
        Element pelement = new Element("xm", obj);
        cache.put(pelement);
        Element pelement2 = cache.get("xm");
        System.out.println("----"+pelement2);
        System.out.println(pelement2.getObjectValue());

        System.out.println(cache.getSize());

        // 7. 刷新缓存
        cache.flush();

        // 8. 关闭缓存管理器
        cacheManager.shutdown();

    }
}
