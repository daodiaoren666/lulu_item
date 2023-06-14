package com.lulu.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lulu.usercenter.model.domain.Team;
import com.lulu.usercenter.model.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.*;


/**
* @author 24174
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2023-06-10 20:10:24
* @Entity com.lulu.usercenter.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {
    List<User> selectTeamUserList(@Param("teamId") long teamId);
}




