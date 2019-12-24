package com.common.util.guavacache;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;

import java.security.Key;
import java.util.concurrent.TimeUnit;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/21 16:56
 */
public class GuavaTest {
    public static void main(String[] args) {
//        LoadingCache<Key, Graph> graphs = CacheBuilder.newBuilder()
//                .maximumSize(1000)
//                .expireAfterWrite(10, TimeUnit.MINUTES)
//                .removalListener(MY_LISTENER)
//                .build(
//                        new CacheLoader<Key, Graph>() {
//                            @Override
//                            public Graph load(Key key) throws AnyException {
//                                return createExpensiveGraph(key);
//                            }
//                        });
//    }
        String aa = "asdfasdf";
        System.out.println(aa.hashCode() % 3);
        System.out.println(Math.floorMod(aa.hashCode(), 3));

        System.out.println("中国".hashCode() % 3);
        System.out.println(Math.floorMod("中国".hashCode(), 3));

    }
}
