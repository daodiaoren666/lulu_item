package com.lulu.usercenter;
import java.util.Date;

import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
class UserCenterApplicationTests {
    @Resource
    private UserService userService;
  private ExecutorService executorService=new ThreadPoolExecutor(20,10000,100000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String newPassword= DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(newPassword);
    }

    /**
     * 使用并发插入数据
     */
    @Test
//    @Scheduled(fixedRate = 5*1000)
    void contextLoads() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int j=0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();

        //分成10组
         int batchSize=10000;
        for (int i = 0; i <=10; i++) {
            List<User> userList=new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假露露");
                user.setUserAccount("fakelulu");
                user.setAvatarUrl("http://a1.qpic.cn/psc?/V13B9pTV0yRnqU/ruAMsa53pVQWN7FLK88i5i8Z6zLs1p9*Ld9G4CjcwrTUvjsQmxtdm9N.uKHHJieDul7Ixv4MGw1NfBOCMaL4a.i7*xiF3MQF.HH6aLlLrBg!/c&ek=1&kp=1&pt=0&tl=1&vuin=2417471135&tm=1686574800&dis_t=1686575957&dis_k=a33c12b48cbfdd0b8cca3a50f3e7f4a7&sce=60-2-2&rf=0-0");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("5445");
                user.setEmail("231651651");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                user.setPlanetCode("11111");
                userList.add(user);
                if(j%batchSize==0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

}
