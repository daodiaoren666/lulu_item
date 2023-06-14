package com.lulu.usercenter;

import com.lulu.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisText {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Test
    void redissonText(){
        RList<Object> list = redissonClient.getList("test-list");
//        list.add("lulu");
        System.out.println(list.get(0));
        list.remove(0);
    }
    @Test
    void text(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("luluString","dog");
        valueOperations.set("luluInt",1);
        valueOperations.set("luluDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("lulu");
        valueOperations.set("luluUser",user);

       Object lulu = valueOperations.get("luluString");
        Assertions.assertTrue("dog".equals((String) lulu));
        lulu = valueOperations.get("luluInt");
        Assertions.assertTrue(1==(Integer) lulu);
        lulu = valueOperations.get("luluDouble");
        Assertions.assertTrue(2.0==(Double) lulu);
        System.out.println(valueOperations.get("luluUser"));
        redisTemplate.delete("luluString");
    }
    @Test
    void testWatchDog(){
        //获取锁
        RLock lock = redissonClient.getLock("lulu::precachejob:docache:lock");
        //只有一个线程能获取到
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(30000);
                System.out.println("getlocak"+Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //只能自己释放自己的锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
