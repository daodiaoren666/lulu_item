package com.lulu.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lulu.usercenter.common.BaseResponse;
import com.lulu.usercenter.common.DeleteRequest;
import com.lulu.usercenter.common.ErrorCode;
import com.lulu.usercenter.common.ResultUtils;
import com.lulu.usercenter.exception.BusinessException;
import com.lulu.usercenter.mapper.TeamMapper;
import com.lulu.usercenter.model.domain.Team;
import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.model.domain.UserTeam;
import com.lulu.usercenter.model.dto.TeamQuery;
import com.lulu.usercenter.model.request.TeamAddRequest;
import com.lulu.usercenter.model.request.TeamQuitRequest;
import com.lulu.usercenter.model.request.TeamUpdateRequest;
import com.lulu.usercenter.model.request.UserJoinTeamRequest;
import com.lulu.usercenter.model.vo.TeamUserVo;
import com.lulu.usercenter.service.TeamService;
import com.lulu.usercenter.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

import com.lulu.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/team")
@CrossOrigin(originPatterns = "*",allowCredentials = "true")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private UserTeamService userTeamService;
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId= teamService.addTeam(team, request);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest==null||deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }
    @GetMapping("/get")
    public BaseResponse<Team> getTeam(Long id) {
        if (id==null||id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }
//    @GetMapping("/list")
//    public BaseResponse<List<Team>> listTeam(TeamQuery teamQuery){
//        if(teamQuery==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        Team team = new Team();
//        BeanUtils.copyProperties(team,teamQuery);
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        List<Team> teamList = teamService.list(queryWrapper);
//        return ResultUtils.success(teamList);
//    }
@GetMapping("/list")
public BaseResponse<List<TeamUserVo>>  listTeam(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    boolean isAdmin = userService.isAdmin(request);

    List<TeamUserVo> teamList = teamService.listTeams(teamQuery,isAdmin);
    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
    //想让用户不登录也能调用这个接口
    try {
         User loginUser = userService.getLoginUser(request);
        //判断当前用户是否已加入队伍
        List<Long> teamIdList=teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());

        queryWrapper.in("teamId",teamIdList);
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //已加入队伍的集合Id
        Set<Long> teamJoinIdSet= userTeamList.stream()
                .map(UserTeam::getTeamId)
                .collect(Collectors.toSet());
        teamList.forEach(team->{
            boolean hasJoin=teamJoinIdSet.contains(team.getId());
            team.setHasJoin(hasJoin);
        });
        //查询已加入的队伍的人数
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamJoinList = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> userTeamGroupList = userTeamJoinList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team->{
            team.setHasJoinNum(userTeamGroupList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
    }catch (Exception e){}

    return ResultUtils.success(teamList);
}
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> pageTeam(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        Page<Team> resultPage = teamService.page(page,queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 查询已加入队伍用户信息
     * @param teamId
     * @return
     */
    @GetMapping("/list/teamUsers")
    public BaseResponse<List<User>> teamUsersList(long teamId){
        if(teamId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
         List<User> userList = teamMapper.selectTeamUserList(teamId);
        return ResultUtils.success(userList);
    }

    /**
     * 用户加入队伍
     * @param userJoinTeamRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody UserJoinTeamRequest userJoinTeamRequest,HttpServletRequest request){
            if(userJoinTeamRequest==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.JoinTeam(userJoinTeamRequest, loginUser);
        return ResultUtils.success(result);
    }
   @PostMapping("/quit")
    public BaseResponse<Boolean>  quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
       boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
       return  ResultUtils.success(result);
   }
     //查询我创建的队伍
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>>  listMyCreateTeam(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(request);
        teamQuery.setUserId(userId);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>>  listMyJoinTeam(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,userId);
        List<UserTeam> list = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = list.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
       List<Long> idList= new ArrayList<>(listMap.keySet());
       teamQuery.setListId(idList);
        //先获取到所有关联信息
//        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }


}
