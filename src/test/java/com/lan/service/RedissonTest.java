package com.lan.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author lan
 */
@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // 数据存在本地 JVM 内存中
        // List<String> list = new ArrayList<String>();
        // list.add("yupi");
        // System.out.println("list:" + list.get(0));
        // list.remove(0);

        // 数据存在 redis 内存中
        RList<String> rList = redissonClient.getList("list");
        // rList.add("yupi");
        System.out.println("rlist:" + rList.get(0));
        rList.remove(0);
        rList.clear();

    }
}
