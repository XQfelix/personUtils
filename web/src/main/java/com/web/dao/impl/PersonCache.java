package com.web.dao.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URL;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2020/1/11 17:28
 */
@Component(value = "personCache")
public class PersonCache {
    private static CacheManager manager = null;
    private static Cache cache;
    static {
        PersonCache cs = new PersonCache();
        cs.init();
    }

    /**
     * 初试化cache
     */
    @PostConstruct
    public void init() {
//        // 关闭tomcat时增加删除回调的钩子
//        System.setProperty(net.sf.ehcache.CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY,"true");
//        URL url = getClass().getResource("/ehcache.xml");
        if(manager==null){
            manager = CacheManager.create("D:/gitPerson/personUtils/web/src/main/resources/ehcache.xml");
//          manager = new CacheManager(url);
            cache = manager.getCache("PersonUtil");
        }
    }

    /**
     * 清除cache
     */
    @PreDestroy
    public void destory() {
        manager.shutdown();
    }

    /**
     * 得到某个key的cache值
     *
     * @param key
     * @return
     */
    public static Element get(String key) {
        Element ret = cache.get(key);
        cache.flush();
        return ret;
    }

    /**
     * 清除key的cache
     *
     * @param key
     */
    public static void remove(String key) {
        cache.remove(key);
        cache.flush();
    }

    /**
     * 设置或更新某个cache值
     *
     * @param element
     */
    public static void put(Element element) {
        cache.put(element);
        cache.flush();
    }

    public static void removeAll(){
        cache.removeAll();
        cache.flush();
    }

    public static void main(String[] args) {
//        PersonCache ps = new PersonCache();
//        Element element = new Element("2222", "33");
//        ps.put(element);
//        ps.destory();
//        Element element1 = new Element("2221", "33");
//        cache.put(element1);
//        Attribute<String> name = cache.getSearchAttribute("key");
//        Query query = cache.createQuery();
//        query.addCriteria(name.ilike("*2*"));
//        query.includeAttribute(name);
//
//        Results results = query.execute();
//    // 获取Results中包含的所有的Result对象
//        List<Result> resultList = results.all();
//        System.out.println(resultList);
    }
}
