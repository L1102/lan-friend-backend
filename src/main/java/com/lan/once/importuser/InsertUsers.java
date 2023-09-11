// package com.lws.yupao.once.importuser;
//
// import com.lws.yupao.mapper.UserMapper;
// import com.lws.yupao.model.domain.User;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.util.StopWatch;
//
// import javax.annotation.Resource;
// import java.util.Date;
//
// /**
//  * @author lan
//  */
// @Component
// public class InsertUsers {
//     @Resource
//     private UserMapper userMapper;
//
//
//     @Scheduled
//     public void doInsertUsers() {
//         StopWatch stopWatch = new StopWatch();
//         stopWatch.start();
//         final int INSERT_NUM = 1000;
//         for (int i = 0; i < INSERT_NUM; i++) {
//             User user = new User();
//             user.setUsername("假人");
//             user.setUserAccount("fakeman");
//             user.setAvatarUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201611%2F13%2F20161113235403_mChxJ.thumb.400_0.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1674204199&t=c7e8acabbf622115a358112235e27b39");
//             user.setGender(0);
//             user.setUserPassword("123456789");
//             user.setPhone("123");
//             user.setEmail("123456@qq.com");
//             user.setUserStatus(0);
//             user.setUserRole(0);
//             user.setCreateTime(new Date());
//             user.setUpdateTime(new Date());
//             user.setIsDelete(0);
//             user.setPlanetCode("99999");
//             user.setTags("[]");
//             userMapper.insert(user);
//         }
//         stopWatch.stop();
//         System.out.println(stopWatch.getLastTaskTimeMillis());
//     }
// }
