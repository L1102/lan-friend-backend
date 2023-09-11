package com.lan.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lan.model.domain.User;
import com.lan.model.vo.UserVO;
import com.lan.service.UserService;
import com.lan.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lan.contant.RedisConstant.USER_RECOMMEND_KEY;
import static com.lan.contant.RedissonConstant.USER_RECOMMEND_LOCK;

/**
 * @author lan
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private List<User> userList = new ArrayList<>();

    /**
     * 每一小时执行
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void doCacheRecommendUsers2() {
        RLock lock = redissonClient.getLock(USER_RECOMMEND_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MICROSECONDS)) {
                System.out.println("开始用户缓存");
                long begin = System.currentTimeMillis() / 1000;
                userList = userService.list();
                for (User user : userList) {
                    for (int i = 1; i <= 5; i++) {
                        Page<UserVO> userVOPage = this.matchUsers(i, user);
                        String key = USER_RECOMMEND_KEY + user.getId() + ":" + i;
                        redisTemplate.opsForValue().set(key, userVOPage);
                    }
                }
                long end = System.currentTimeMillis() / 1000;
                System.out.println("用户缓存结束，耗时 " + (end - begin) + " 秒");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    public Page<UserVO> matchUsers(long currentPage, User loginUser) {
        String tags = loginUser.getTags();
        if (tags == null) {
            return userService.userPage(currentPage);
            // Page<User> page = userService.page(new Page<>(1, 20));
            // Page<UserVO> userVOPage = new Page<>();
            // BeanUtils.copyProperties(page, userVOPage);
            // return userVOPage;
        }
        // QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // queryWrapper.select("id", "tags");
        // queryWrapper.isNotNull("tags");
        // List<User> userList = userService.list(queryWrapper);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 --》相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或为当前用户
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .collect(Collectors.toList());

        //截取currentPage所需的List
        ArrayList<Pair<User, Long>> finalUserPairList = new ArrayList<>();
        int begin = (int) ((currentPage - 1) * 8);
        int end = (int) (((currentPage - 1) * 8) + 8) - 1;
        if (topUserPairList.size() < end) {
            //剩余数量
            int temp = (int) (topUserPairList.size() - begin);
            if (temp <= 0) {
                return new Page<>();
            }
            for (int i = begin; i <= begin + temp - 1; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        } else {
            for (int i = begin; i < end; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        }

        List<Long> userIdList = finalUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        List<UserVO> userVOList = userService.list(userQueryWrapper).stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        userVOPage.setCurrent(currentPage);
        userVOPage.setSize(userVOList.size());
        userVOPage.setTotal(userVOList.size());
        return userVOPage;
    }
}
