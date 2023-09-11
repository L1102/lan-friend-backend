package com.lan;

import com.lan.mapper.UserMapper;
import com.lan.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootTest
@Slf4j
class FriendApplicationTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assertions.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

    @Test
    void test() throws NoSuchAlgorithmException {
        String s = DigestUtils.md5DigestAsHex(("abcd" + "password").getBytes());
        System.out.println(s);
    }

}
