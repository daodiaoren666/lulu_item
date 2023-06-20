package com.lulu.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.util.List;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = 8615852534001666594L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;
    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
    /**
     * 入队用户列表
     */
  private   List<UserVo> userList;
    /**
     * 创建人信息
     */
    private  UserVo CreateUser;
    /**
     * 用户是否加入该队伍
     */
    private boolean hasJoin=false;
    /**
     * 队伍的已加入人数
     */
    private Integer hasJoinNum;
}
