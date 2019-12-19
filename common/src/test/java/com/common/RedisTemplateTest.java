package com.common;

import com.common.util.redisTemplate.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/19 11:53
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTest {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test(){
        redisUtil.set("11", "123");
        System.out.println(redisUtil.get("11"));
    }

}
