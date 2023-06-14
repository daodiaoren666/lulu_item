package com.lulu.usercenter.service;

import com.lulu.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 用户服务测试
 *
 * @author lulu
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {

        for (int i = 0; i <6 ; i++) {
            User user = new User();
            user.setUsername("公主");
            user.setUserAccount("qaq666");
            user.setAvatarUrl("http://a1.qpic.cn/psc?/V13B9pTV0yRnqU/ruAMsa53pVQWN7FLK88i5g6fipPP8wX9NzPnnCApr54TwZfA.lpD8Ks0vK170XNnXkRw5XlisprSVb1E9fMaI5sLrTLVFZwVYK2dndCqYRI!/c&ek=1&kp=1&pt=0&bo=pgb8BKYG*AQWECA!&tl=1&vuin=2417471135&tm=1686574800&dis_t=1686575957&dis_k=dd574ba9ef6833bd1892a22b5c1a30ce&sce=60-2-2&rf=0-0");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("456");
            boolean result = userService.save(user);
            System.out.println(user.getId());
            Assertions.assertTrue(result);
        }

    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("doglulu");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    @Test
    public void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    @Test
    public void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    void userRegister() {
        String userAccount = "lulu";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "lulu";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "doglulu";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
        userAccount = "lulu";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result);
    }
  @Test
    public void UserTagsName(){
      List<String> tagsList= Arrays.asList("男");
      List<User> userList = userService.searchUserByTagsMemory(tagsList);
      for (User user : userList) {
          System.out.println(user);
      }

  }
}