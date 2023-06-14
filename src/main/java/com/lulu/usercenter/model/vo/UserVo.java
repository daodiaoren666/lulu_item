package com.lulu.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 7214570893631799874L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
     */

    private String username;

    /**
     * 账号
     */

    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 角色 0--表示普通用户 1表示管理员
     */
    private Integer userRole;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 星球编号
     */
    private String planetCode;

}
