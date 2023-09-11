package com.lan.service;

import com.lan.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lan
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    private ExecutorService executorService =
            new ThreadPoolExecutor(8, 500, 10000,
                    TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假人");
            user.setUserAccount("fakeman");
            user.setAvatarUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201611%2F13%2F20161113235403_mChxJ.thumb.400_0.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1674204199&t=c7e8acabbf622115a358112235e27b39");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("123");
            user.setEmail("123456@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDelete(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int batchSize = 5000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假人");
                user.setUserAccount("fakeman");
                user.setAvatarUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201611%2F13%2F20161113235403_mChxJ.thumb.400_0.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1674204199&t=c7e8acabbf622115a358112235e27b39");
                user.setGender(0);
                user.setUserPassword("123456789");
                user.setPhone("123");
                user.setEmail("123456@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setIsDelete(0);
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                System.out.println("当前线程= " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(completableFuture);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}
