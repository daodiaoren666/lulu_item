package com.lulu.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lulu.usercenter.model.domain.Team;
import com.lulu.usercenter.model.domain.User;
import com.lulu.usercenter.model.dto.TeamQuery;
import com.lulu.usercenter.model.request.TeamQuitRequest;
import com.lulu.usercenter.model.request.TeamUpdateRequest;
import com.lulu.usercenter.model.request.UserJoinTeamRequest;
import com.lulu.usercenter.model.vo.TeamUserVo;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24174
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-06-10 20:10:24
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team,HttpServletRequest request);

    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    boolean JoinTeam(UserJoinTeamRequest userJoinTeamRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);
}
