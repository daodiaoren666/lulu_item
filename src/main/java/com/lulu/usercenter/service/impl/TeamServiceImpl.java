package com.lulu.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lulu.usercenter.common.ErrorCode;
import com.lulu.usercenter.exception.BusinessException;
import com.lulu.usercenter.model.domain.Team;
import com.lulu.usercenter.mapper.TeamMapper;
import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.model.domain.UserTeam;
import com.lulu.usercenter.model.dto.TeamQuery;
import com.lulu.usercenter.model.enums.TeamStatusEnum;
import com.lulu.usercenter.model.request.TeamQuitRequest;
import com.lulu.usercenter.model.request.TeamUpdateRequest;
import com.lulu.usercenter.model.request.UserJoinTeamRequest;
import com.lulu.usercenter.model.vo.TeamUserVo;
import com.lulu.usercenter.model.vo.UserVo;
import com.lulu.usercenter.service.TeamService;
import com.lulu.usercenter.service.UserService;
import com.lulu.usercenter.service.UserTeamService;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 24174
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-06-10 20:10:24
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, HttpServletRequest request) {
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        final long userId = loginUser.getId();
//        1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
//        3. 校验信息
//        1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数错误");
        }
//        2. 队伍标题 <= 20
        String teamName = team.getName();
        if (teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题过长");
        }
//        3. 描述 <= 512
        String teamDescription = team.getDescription();
        if (StringUtils.isNotBlank(teamDescription) && teamDescription.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述错误");
        }
//        4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足");
        }
//        5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            String password = team.getPassword();
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
//        6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
//        7. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
//        4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //        5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        Long teamId = team.getId();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加用户队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {

        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null) {
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like(Team::getName, name);
            }
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq(Team::getId, id);
            }
            //根据描述和队伍名称联合查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.like(Team::getName, searchText).or().like(Team::getDescription, searchText);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like(Team::getDescription, description);
            }
            //展示不过期的信息或者为空的
            queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date()).or().isNull(Team::getExpireTime));
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq(Team::getUserId, userId);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            //根据状态来查询
            //判断是否为管理员？ 是否有权限查看私密队伍
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq(Team::getStatus, statusEnum.getValue());
        }
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            BeanUtils.copyProperties(user, userVo);
            teamUserVo.setCreateUser(userVo);
            teamUserVoList.add(teamUserVo);
        }

        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser) {
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id==null||id<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if(!Objects.equals(loginUser.getId(), oldTeam.getUserId()) &&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEumByValue(status);
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw  new BusinessException(ErrorCode.PARAMS_ERROR,"加密队伍必须设置密码");
            }
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);
       return teamService.updateById(team);
    }

    @Override
    public boolean JoinTeam(UserJoinTeamRequest userJoinTeamRequest, User loginUser) {
        if(userJoinTeamRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer id = userJoinTeamRequest.getId();
        if(id==null||id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(id);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if(expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"禁止加入私有队伍");
        }
        String password = userJoinTeamRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) ||!team.getPassword().equals(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        //用户最多加入5个队伍
        Long userId = loginUser.getId();
        Long teamId = team.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,userId);
        long teamHasNum = userTeamService.count(queryWrapper);
        if(teamHasNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多加入5个队伍");
        }
        
        //不能重复加入已加入的队伍
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,userId);
        queryWrapper.eq(UserTeam::getTeamId,teamId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if(hasUserJoinTeam>0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户已加入该队伍");
        }
        //判断队伍是否未满？
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,teamId);
        Integer maxNum = team.getMaxNum();
        long userHasNum = userTeamService.count(queryWrapper);
        if(maxNum<=userHasNum){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已满");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
       return userTeamService.save(userTeam);
    }

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //检验我是否已加入队伍
        Long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long hasTeamNum = this.countTeamUserByTeamId(teamId);
        //如果队伍只剩下一个人 队伍解散
        if (hasTeamNum == 1) {
            //删除队伍用户表
            this.removeById(teamId);
            return userTeamService.remove(queryWrapper);
        }
        //怎么判断是不是队长呢？
        //在这先假定创建者就是队长  没用房主的概念  如果队长退出了 则顺位转交下一位
        //如果是队长
        if (userId.equals(team.getUserId()) ) {
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //权力转移第二早加入的用户
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
            userTeamLambdaQueryWrapper.last("order by id asc  limit 2");
            List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
            if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            UserTeam nextUserTeam = userTeamList.get(1);
            Long nextTeamLeaderId = nextUserTeam.getUserId();
            //变更当前的队长
            Team updateTeam = new Team();
            updateTeam.setId(teamId);
            updateTeam.setUserId(nextTeamLeaderId);
            boolean result = this.updateById(updateTeam);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "变更队长失败");
            }
        }
          //移除关系
        return userTeamService.remove(queryWrapper);
    }

    private long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();;
        queryWrapper.eq("teamId", teamId);
     return    userTeamService.count(queryWrapper);
    }


}




