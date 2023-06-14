package com.lulu.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lulu.usercenter.common.ErrorCode;
import com.lulu.usercenter.exception.BusinessException;
import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.service.UserService;
import com.lulu.usercenter.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lulu.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.lulu.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author lulu
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "lulu";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public int userUpdate(User user, User loginUser) {
        //如果要修改的用户id错误 直接抛异常
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //仅管理员和自己可修改
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //查询要修改的用户id 是否存在
        User oldUser = userMapper.selectById(userId);
        if(oldUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
       return   userMapper.updateById(user);

    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }


    /**
     * 根据 Sql查询 标签搜索用户
     *
     * @param tagsList 用户所拥有的标签列表
     * @return
     */

    public List<User> searchUserByTags(List<String> tagsList) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isEmpty(tagsList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);//请求参数为空
        }
        long millis = System.currentTimeMillis();//开始时间

        for (String tagName : tagsList) {
            queryWrapper = queryWrapper.like(User::getTags, tagName);
        }
        long millis1 = System.currentTimeMillis();
        log.info("sql 时间为" + (millis1 - millis));
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据内存 查询 标签搜索用户
     *
     * @param tagsList 用户所拥有的标签列表
     * @return
     */
    //
    public List<User> searchUserByTagsMemory(List<String> tagsList) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(wrapper);//拿到所有用户
        if (CollectionUtils.isEmpty(tagsList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);//请求参数为空
        }
        long millis = System.currentTimeMillis();//开始时间
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            Set<String> tagsListTemp = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
            }.getType());
            tagsListTemp = Optional.ofNullable(tagsListTemp).orElse(new HashSet<>());
            for (String tagsName : tagsList) {
                if (!tagsListTemp.contains(tagsName)) {
                    return false;
                }
            }
            long millis1 = System.currentTimeMillis();
            log.info("memory 时间为" + (millis1 - millis));
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;

    }

    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 使用redis缓存获取推荐用户列表
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    @Override
    public Page<User> RedisRecommendUser(long pageSize, long pageNum,HttpServletRequest request) {
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        String RedisKey = String.format("lulu:user:recommend:userId:%s", loginUser.getId());
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        //如果有缓存 直接读取缓存
        Page<User> userPage = (Page<User>) opsForValue.get(RedisKey);
        if(userPage!=null){
            return userPage;
        }
        //如果没有缓存  从数据库中获取 并插入缓存信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize),queryWrapper);
        //写缓存
        try {
            opsForValue.set(RedisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("redis set key err",e);
        }
           return userPage;
    }

}




