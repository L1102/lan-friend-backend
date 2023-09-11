package com.lan.service;

import com.lan.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author lan
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String, java.io.Serializable> redisTemplate;

    @Test
    void test() {
        ValueOperations<String, java.io.Serializable> valueOperations = redisTemplate.opsForValue();
        // 增加
        valueOperations.set("lanString", "哈哈");
        valueOperations.set("lanInt", 123);
        valueOperations.set("lanDouble", 110.12);
        User user = new User();
        user.setId(1L);
        user.setUsername("lan");
        valueOperations.set("lanUser", user);

        // 查询
        Object lan = valueOperations.get("lanString");
        Assertions.assertEquals("哈哈", lan);
        lan = valueOperations.get("lanInt");
        Assertions.assertEquals(123, lan);
        lan = valueOperations.get("lanDouble");
        Assertions.assertEquals(110.12, lan);
        System.out.println(valueOperations.get("lanUser"));
    }

    @Test
    void get() {
        ValueOperations<String, java.io.Serializable> valueOperations = redisTemplate.opsForValue();
        // 查询
        Object lan = valueOperations.get("lanString");
        Assertions.assertEquals("哈哈", lan);
    }

    @Test
    void delete() {
        ValueOperations<String, java.io.Serializable> valueOperations = redisTemplate.opsForValue();
        Set<String> keys = redisTemplate.keys("*");
        assert keys != null;
        redisTemplate.delete(keys);
    }


}
