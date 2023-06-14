package com.lulu.usercenter.ocne;

import com.lulu.usercenter.mapper.UserMapper;
import com.lulu.usercenter.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {
@Resource

private UserMapper userMapper;

    public static void main(String[] args) {
        new InsertUsers().contextLoads();
    }
//    @Scheduled(fixedRate = 5*1000)
    void contextLoads() {
        StopWatch watch = new StopWatch();
        watch.start();
        final int INSERT_NUM=1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("小神龙");
            user.setUserAccount("red");
            user.setAvatarUrl("https://pic4.zhimg.com/80/v2-8e09c59051466340b39501e20ff30607_720w.webp");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("231551");
            user.setEmail("5156116");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            user.setPlanetCode("11158");
            userMapper.insert(user);
            System.out.println("插入成功");
        }
        watch.stop();
        System.out.println(watch.getTotalTimeMillis());

    }
}
