package com.lulu.usercenter.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
@Resource
private RedisTemplate redisTemplate;
@Resource
private UserService userService;

//重点用户
    private List<Long> mainUserList= Arrays.asList(11L);
   @Scheduled(cron = "0 21 18 * * *")
    public void doCacheRecommendUser(){
       for (Long userId : mainUserList) {
           String RedisKey = String.format("lulu:user:recommend:userId:%s",userId);
           ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
           QueryWrapper<User> queryWrapper = new QueryWrapper<>();
           Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
           //写缓存
           try {
               opsForValue.set(RedisKey,userPage,30000, TimeUnit.MILLISECONDS);
           } catch (Exception e) {
               log.info("redis set key err",e);
           }
       }

   }
}
